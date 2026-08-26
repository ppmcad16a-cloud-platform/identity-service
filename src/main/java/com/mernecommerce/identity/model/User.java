package com.mernecommerce.identity.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

import com.mernecommerce.identity.constants.EmailProvider;
import com.mernecommerce.identity.constants.Roles;

// A logged-in account: buyer (default), merchant, or admin, distinguished by
// `role`. Owned by identity_server — collection "users", shared across every
// microservice's own trimmed copy of this schema. @JsonProperty("_id") on
// `id` matches Mongoose's default JSON shape (`_id`, not `id`), which
// client/ relies on directly in a few places (e.g. Address's `_id`).
@Document(collection = "users")
public class User {

    @Id
    @JsonProperty("_id")
    private String id;

    private String email;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String password;
    private String merchant;
    private String provider = EmailProvider.EMAIL;
    private String avatar;
    private String role = Roles.MEMBER;
    private String resetPasswordToken;
    private Instant resetPasswordExpires;
    private Instant updated;
    private Instant created = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getMerchant() { return merchant; }
    public void setMerchant(String merchant) { this.merchant = merchant; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getResetPasswordToken() { return resetPasswordToken; }
    public void setResetPasswordToken(String resetPasswordToken) { this.resetPasswordToken = resetPasswordToken; }

    public Instant getResetPasswordExpires() { return resetPasswordExpires; }
    public void setResetPasswordExpires(Instant resetPasswordExpires) { this.resetPasswordExpires = resetPasswordExpires; }

    public Instant getUpdated() { return updated; }
    public void setUpdated(Instant updated) { this.updated = updated; }

    public Instant getCreated() { return created; }
    public void setCreated(Instant created) { this.created = created; }
}
