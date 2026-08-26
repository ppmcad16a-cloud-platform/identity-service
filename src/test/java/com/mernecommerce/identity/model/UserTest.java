package com.mernecommerce.identity.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void gettersAndSettersRoundTrip() {
        User user = new User();
        Instant now = Instant.now();

        user.setId("id1");
        user.setEmail("a@b.com");
        user.setPhoneNumber("555-1234");
        user.setFirstName("First");
        user.setLastName("Last");
        user.setPassword("hashed");
        user.setMerchant("merchant1");
        user.setProvider("Email");
        user.setAvatar("avatar.png");
        user.setRole("ROLE MEMBER");
        user.setResetPasswordToken("token");
        user.setResetPasswordExpires(now);
        user.setUpdated(now);
        user.setCreated(now);

        assertThat(user.getId()).isEqualTo("id1");
        assertThat(user.getEmail()).isEqualTo("a@b.com");
        assertThat(user.getPhoneNumber()).isEqualTo("555-1234");
        assertThat(user.getFirstName()).isEqualTo("First");
        assertThat(user.getLastName()).isEqualTo("Last");
        assertThat(user.getPassword()).isEqualTo("hashed");
        assertThat(user.getMerchant()).isEqualTo("merchant1");
        assertThat(user.getProvider()).isEqualTo("Email");
        assertThat(user.getAvatar()).isEqualTo("avatar.png");
        assertThat(user.getRole()).isEqualTo("ROLE MEMBER");
        assertThat(user.getResetPasswordToken()).isEqualTo("token");
        assertThat(user.getResetPasswordExpires()).isEqualTo(now);
        assertThat(user.getUpdated()).isEqualTo(now);
        assertThat(user.getCreated()).isEqualTo(now);
    }

    @Test
    void defaults() {
        User user = new User();
        assertThat(user.getProvider()).isEqualTo("Email");
        assertThat(user.getRole()).isEqualTo("ROLE MEMBER");
        assertThat(user.getCreated()).isNotNull();
    }
}
