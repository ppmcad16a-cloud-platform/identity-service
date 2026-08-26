package com.mernecommerce.identity.security;

import com.mernecommerce.identity.model.User;
import com.mernecommerce.identity.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

// Equivalent of middleware/auth.js (passport-jwt) across the Node
// microservices: reads `Authorization: Bearer <token>`, verifies it, and —
// on success — loads the User and sets it as the request's principal so
// controllers can read it via @AuthenticationPrincipal, mirroring `req.user`.
// A missing/invalid token is NOT rejected here; it just leaves the request
// unauthenticated, and SecurityConfig's per-route rules decide whether that
// 401s (same division of responsibility as Express: this filter only does
// authentication, not authorization).
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            try {
                String userId = jwtService.extractUserId(token);
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    var authorities = List.of(new SimpleGrantedAuthority(user.getRole()));
                    var authToken = new UsernamePasswordAuthenticationToken(user, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception ignored) {
                // Invalid/expired token or user no longer exists: leave
                // unauthenticated, same as the Node services' behavior.
            }
        }

        filterChain.doFilter(request, response);
    }
}
