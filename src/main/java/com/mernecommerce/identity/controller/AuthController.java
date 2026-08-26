package com.mernecommerce.identity.controller;

import com.mernecommerce.identity.constants.EmailProvider;
import com.mernecommerce.identity.model.User;
import com.mernecommerce.identity.repository.UserRepository;
import com.mernecommerce.identity.security.JwtService;
import com.mernecommerce.identity.util.Json;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;

// Port of routes/api/auth.js: email/password auth (login, register) plus
// the two reset-password flows (forgot -> emailed token -> reset/{token},
// and change-password-while-logged-in via POST /reset). Every handler
// mirrors the original's manual field checks and exact error strings/
// response shapes, since client/app/utils/error.js surfaces `error.message`
// from 400 responses directly to the user.
@RestController
@RequestMapping("/${BASE_API_URL:api}/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private static final SecureRandom RANDOM = new SecureRandom();

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String password = body.get("password");

            if (isBlank(email)) {
                return badRequest("You must enter an email address.");
            }
            if (isBlank(password)) {
                return badRequest("You must enter a password.");
            }

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return badRequest("No user found for this email address.");
            }
            User user = userOpt.get();

            if (user.getProvider() != null && !user.getProvider().equals(EmailProvider.EMAIL)) {
                return badRequest("That email address is already in use using " + user.getProvider() + " provider.");
            }

            if (!passwordEncoder.matches(password, user.getPassword())) {
                return ResponseEntity.status(400).body(Json.of("success", false, "error", "Password Incorrect"));
            }

            return authSuccessResponse(user);
        } catch (Exception e) {
            return genericError();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String firstName = body.get("firstName");
            String lastName = body.get("lastName");
            String password = body.get("password");

            if (isBlank(email)) {
                return badRequest("You must enter an email address.");
            }
            if (isBlank(firstName) || isBlank(lastName)) {
                return badRequest("You must enter your full name.");
            }
            if (isBlank(password)) {
                return badRequest("You must enter a password.");
            }

            if (userRepository.findByEmail(email).isPresent()) {
                return badRequest("That email address is already in use.");
            }

            User user = new User();
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setPassword(passwordEncoder.encode(password));
            User saved = userRepository.save(user);

            return authSuccessResponse(saved);
        } catch (Exception e) {
            return genericError();
        }
    }

    @PostMapping("/forgot")
    public ResponseEntity<?> forgot(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            if (isBlank(email)) {
                return badRequest("You must enter an email address.");
            }

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return badRequest("No user found for this email address.");
            }

            User user = userOpt.get();
            user.setResetPasswordToken(randomHexToken());
            user.setResetPasswordExpires(Instant.now().plus(1, ChronoUnit.HOURS));
            userRepository.save(user);

            return ResponseEntity.ok(Json.of(
                    "success", true,
                    "message", "Please check your email for the link to reset your password."
            ));
        } catch (Exception e) {
            return genericError();
        }
    }

    @PostMapping("/reset/{token}")
    public ResponseEntity<?> resetWithToken(@PathVariable String token, @RequestBody Map<String, String> body) {
        try {
            String password = body.get("password");
            if (isBlank(password)) {
                return badRequest("You must enter a password.");
            }

            Optional<User> userOpt = userRepository
                    .findByResetPasswordTokenAndResetPasswordExpiresAfter(token, Instant.now());
            if (userOpt.isEmpty()) {
                return badRequest("Your token has expired. Please attempt to reset your password again.");
            }

            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(password));
            user.setResetPasswordToken(null);
            user.setResetPasswordExpires(null);
            userRepository.save(user);

            return ResponseEntity.ok(Json.of(
                    "success", true,
                    "message", "Password changed successfully. Please login with your new password."
            ));
        } catch (Exception e) {
            return genericError();
        }
    }

    // "Change my password" for an already-authenticated user — distinct
    // from resetWithToken above despite the similar path (no {token}
    // segment here). Requires the caller's *current* password.
    @PostMapping("/reset")
    public ResponseEntity<?> resetSelf(@AuthenticationPrincipal User principal, @RequestBody Map<String, String> body) {
        try {
            String password = body.get("password");
            String confirmPassword = body.get("confirmPassword");

            String email = principal != null ? principal.getEmail() : null;
            if (isBlank(email)) {
                return ResponseEntity.status(401).body("Unauthenticated");
            }
            if (isBlank(password)) {
                return badRequest("You must enter a password.");
            }

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                return badRequest("That email address is already in use.");
            }
            User user = userOpt.get();

            if (!passwordEncoder.matches(password, user.getPassword())) {
                return badRequest("Please enter your correct old password.");
            }

            user.setPassword(passwordEncoder.encode(confirmPassword));
            userRepository.save(user);

            return ResponseEntity.ok(Json.of(
                    "success", true,
                    "message", "Password changed successfully. Please login with your new password."
            ));
        } catch (Exception e) {
            return genericError();
        }
    }

    private ResponseEntity<?> authSuccessResponse(User user) {
        String token = "Bearer " + jwtService.generateToken(user.getId());
        return ResponseEntity.ok(Json.of(
                "success", true,
                "token", token,
                "user", Json.of(
                        "id", user.getId(),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "email", user.getEmail(),
                        "role", user.getRole()
                )
        ));
    }

    private static String randomHexToken() {
        byte[] buffer = new byte[48];
        RANDOM.nextBytes(buffer);
        StringBuilder sb = new StringBuilder();
        for (byte b : buffer) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static ResponseEntity<?> badRequest(String message) {
        return ResponseEntity.status(400).body(Json.of("error", message));
    }

    private static ResponseEntity<?> genericError() {
        return badRequest("Your request could not be processed. Please try again.");
    }
}
