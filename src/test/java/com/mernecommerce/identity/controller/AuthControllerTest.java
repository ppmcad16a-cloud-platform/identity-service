package com.mernecommerce.identity.controller;

import com.mernecommerce.identity.model.User;
import com.mernecommerce.identity.repository.UserRepository;
import com.mernecommerce.identity.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class AuthControllerTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtService jwtService = mock(JwtService.class);
    private final AuthController controller = new AuthController(userRepository, passwordEncoder, jwtService);

    private static Map<String, String> mapOf(Object... kv) {
        Map<String, String> m = new HashMap<>();
        for (int i = 0; i < kv.length; i += 2) {
            m.put((String) kv[i], (String) kv[i + 1]);
        }
        return m;
    }

    private static User user(String id, String email, String password, String role, String provider) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setFirstName("First");
        u.setLastName("Last");
        u.setPassword(password);
        u.setRole(role);
        u.setProvider(provider);
        return u;
    }

    // ---- login ----

    @Test
    void loginMissingEmail() {
        ResponseEntity<?> res = controller.login(mapOf("password", "secret1"));
        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(((Map<String, Object>) res.getBody())).containsEntry("error", "You must enter an email address.");
    }

    @Test
    void loginBlankEmail() {
        ResponseEntity<?> res = controller.login(mapOf("email", "", "password", "secret1"));
        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void loginMissingPassword() {
        ResponseEntity<?> res = controller.login(mapOf("email", "a@b.com"));
        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(((Map<String, Object>) res.getBody())).containsEntry("error", "You must enter a password.");
    }

    @Test
    void loginUserNotFound() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());
        ResponseEntity<?> res = controller.login(mapOf("email", "a@b.com", "password", "secret1"));
        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(((Map<String, Object>) res.getBody())).containsEntry("error", "No user found for this email address.");
    }

    @Test
    void loginWithNullProviderSkipsProviderCheck() {
        User u = user("u1", "a@b.com", "hash", "ROLE MEMBER", null);
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("secret1", "hash")).thenReturn(true);
        when(jwtService.generateToken("u1")).thenReturn("jwt-token");

        ResponseEntity<?> res = controller.login(mapOf("email", "a@b.com", "password", "secret1"));

        assertThat(res.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void loginWithNonEmailProviderRejected() {
        User u = user("u1", "a@b.com", "hash", "ROLE MEMBER", "Google");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(u));

        ResponseEntity<?> res = controller.login(mapOf("email", "a@b.com", "password", "secret1"));

        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(((Map<String, Object>) res.getBody()).get("error"))
                .isEqualTo("That email address is already in use using Google provider.");
    }

    @Test
    void loginWrongPassword() {
        User u = user("u1", "a@b.com", "hash", "ROLE MEMBER", "Email");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);

        ResponseEntity<?> res = controller.login(mapOf("email", "a@b.com", "password", "wrong"));

        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(((Map<String, Object>) res.getBody())).containsEntry("success", false).containsEntry("error", "Password Incorrect");
    }

    @Test
    void loginSuccess() {
        User u = user("u1", "a@b.com", "hash", "ROLE ADMIN", "Email");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("secret1", "hash")).thenReturn(true);
        when(jwtService.generateToken("u1")).thenReturn("jwt-token");

        ResponseEntity<?> res = controller.login(mapOf("email", "a@b.com", "password", "secret1"));

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertThat(body).containsEntry("success", true).containsEntry("token", "Bearer jwt-token");
        Map<String, Object> userBody = (Map<String, Object>) body.get("user");
        assertThat(userBody).containsEntry("id", "u1").containsEntry("role", "ROLE ADMIN");
    }

    @Test
    void loginHandlesUnexpectedException() {
        when(userRepository.findByEmail(anyString())).thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> res = controller.login(mapOf("email", "a@b.com", "password", "secret1"));

        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(((Map<String, Object>) res.getBody()))
                .containsEntry("error", "Your request could not be processed. Please try again.");
    }

    // ---- register ----

    @Test
    void registerMissingEmail() {
        ResponseEntity<?> res = controller.register(mapOf("firstName", "F", "lastName", "L", "password", "secret1"));
        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void registerMissingFirstName() {
        ResponseEntity<?> res = controller.register(mapOf("email", "a@b.com", "lastName", "L", "password", "secret1"));
        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(((Map<String, Object>) res.getBody())).containsEntry("error", "You must enter your full name.");
    }

    @Test
    void registerMissingLastName() {
        ResponseEntity<?> res = controller.register(mapOf("email", "a@b.com", "firstName", "F", "password", "secret1"));
        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void registerMissingPassword() {
        ResponseEntity<?> res = controller.register(mapOf("email", "a@b.com", "firstName", "F", "lastName", "L"));
        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void registerEmailAlreadyInUse() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user("u1", "a@b.com", "h", "ROLE MEMBER", "Email")));

        ResponseEntity<?> res = controller.register(mapOf("email", "a@b.com", "firstName", "F", "lastName", "L", "password", "secret1"));

        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(((Map<String, Object>) res.getBody())).containsEntry("error", "That email address is already in use.");
    }

    @Test
    void registerSuccess() {
        when(userRepository.findByEmail("new@b.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret1")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("new-id");
            return u;
        });
        when(jwtService.generateToken("new-id")).thenReturn("jwt-token");

        ResponseEntity<?> res = controller.register(mapOf("email", "new@b.com", "firstName", "F", "lastName", "L", "password", "secret1"));

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerHandlesUnexpectedException() {
        when(userRepository.findByEmail(anyString())).thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> res = controller.register(mapOf("email", "a@b.com", "firstName", "F", "lastName", "L", "password", "secret1"));

        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }

    // ---- forgot ----

    @Test
    void forgotMissingEmail() {
        ResponseEntity<?> res = controller.forgot(mapOf());
        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void forgotUserNotFound() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());
        ResponseEntity<?> res = controller.forgot(mapOf("email", "a@b.com"));
        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void forgotSuccess() {
        User u = user("u1", "a@b.com", "hash", "ROLE MEMBER", "Email");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(u));

        ResponseEntity<?> res = controller.forgot(mapOf("email", "a@b.com"));

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        assertThat(u.getResetPasswordToken()).isNotBlank();
        assertThat(u.getResetPasswordExpires()).isNotNull();
        verify(userRepository).save(u);
    }

    @Test
    void forgotHandlesUnexpectedException() {
        when(userRepository.findByEmail(anyString())).thenThrow(new RuntimeException("boom"));
        ResponseEntity<?> res = controller.forgot(mapOf("email", "a@b.com"));
        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }

    // ---- resetWithToken ----

    @Test
    void resetWithTokenMissingPassword() {
        ResponseEntity<?> res = controller.resetWithToken("tok", mapOf());
        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void resetWithTokenExpiredOrInvalid() {
        when(userRepository.findByResetPasswordTokenAndResetPasswordExpiresAfter(eq("tok"), any()))
                .thenReturn(Optional.empty());

        ResponseEntity<?> res = controller.resetWithToken("tok", mapOf("password", "newpass1"));

        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(((Map<String, Object>) res.getBody()))
                .containsEntry("error", "Your token has expired. Please attempt to reset your password again.");
    }

    @Test
    void resetWithTokenSuccess() {
        User u = user("u1", "a@b.com", "oldhash", "ROLE MEMBER", "Email");
        u.setResetPasswordToken("tok");
        when(userRepository.findByResetPasswordTokenAndResetPasswordExpiresAfter(eq("tok"), any()))
                .thenReturn(Optional.of(u));
        when(passwordEncoder.encode("newpass1")).thenReturn("newhash");

        ResponseEntity<?> res = controller.resetWithToken("tok", mapOf("password", "newpass1"));

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        assertThat(u.getPassword()).isEqualTo("newhash");
        assertThat(u.getResetPasswordToken()).isNull();
        assertThat(u.getResetPasswordExpires()).isNull();
    }

    @Test
    void resetWithTokenHandlesUnexpectedException() {
        when(userRepository.findByResetPasswordTokenAndResetPasswordExpiresAfter(anyString(), any()))
                .thenThrow(new RuntimeException("boom"));
        ResponseEntity<?> res = controller.resetWithToken("tok", mapOf("password", "newpass1"));
        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }

    // ---- resetSelf ----

    @Test
    void resetSelfNoPrincipal() {
        ResponseEntity<?> res = controller.resetSelf(null, mapOf("password", "old1", "confirmPassword", "new1"));
        assertThat(res.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void resetSelfPrincipalWithBlankEmail() {
        User principal = user("u1", "", "hash", "ROLE MEMBER", "Email");
        ResponseEntity<?> res = controller.resetSelf(principal, mapOf("password", "old1", "confirmPassword", "new1"));
        assertThat(res.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void resetSelfMissingPassword() {
        User principal = user("u1", "a@b.com", "hash", "ROLE MEMBER", "Email");
        ResponseEntity<?> res = controller.resetSelf(principal, mapOf("confirmPassword", "new1"));
        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void resetSelfUserLookupEmpty() {
        User principal = user("u1", "a@b.com", "hash", "ROLE MEMBER", "Email");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());

        ResponseEntity<?> res = controller.resetSelf(principal, mapOf("password", "old1", "confirmPassword", "new1"));

        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(((Map<String, Object>) res.getBody())).containsEntry("error", "That email address is already in use.");
    }

    @Test
    void resetSelfWrongOldPassword() {
        User principal = user("u1", "a@b.com", "hash", "ROLE MEMBER", "Email");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(principal));
        when(passwordEncoder.matches("old1", "hash")).thenReturn(false);

        ResponseEntity<?> res = controller.resetSelf(principal, mapOf("password", "old1", "confirmPassword", "new1"));

        assertThat(res.getStatusCode().value()).isEqualTo(400);
        assertThat(((Map<String, Object>) res.getBody())).containsEntry("error", "Please enter your correct old password.");
    }

    @Test
    void resetSelfSuccess() {
        User principal = user("u1", "a@b.com", "hash", "ROLE MEMBER", "Email");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(principal));
        when(passwordEncoder.matches("old1", "hash")).thenReturn(true);
        when(passwordEncoder.encode("new1")).thenReturn("newhash");

        ResponseEntity<?> res = controller.resetSelf(principal, mapOf("password", "old1", "confirmPassword", "new1"));

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        assertThat(principal.getPassword()).isEqualTo("newhash");
    }

    @Test
    void resetSelfHandlesUnexpectedException() {
        User principal = user("u1", "a@b.com", "hash", "ROLE MEMBER", "Email");
        when(userRepository.findByEmail(anyString())).thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> res = controller.resetSelf(principal, mapOf("password", "old1", "confirmPassword", "new1"));

        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }
}
