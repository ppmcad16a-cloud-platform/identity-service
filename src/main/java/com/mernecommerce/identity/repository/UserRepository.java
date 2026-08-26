package com.mernecommerce.identity.repository;

import com.mernecommerce.identity.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndResetPasswordToken(String email, String resetPasswordToken);
    Optional<User> findByResetPasswordTokenAndResetPasswordExpiresAfter(String resetPasswordToken, Instant now);
}
