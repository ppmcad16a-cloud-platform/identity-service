package com.mernecommerce.identity.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AddressTest {

    @Test
    void gettersAndSettersRoundTrip() {
        Address address = new Address();
        Instant now = Instant.now();

        address.setId("addr1");
        address.setUser("user1");
        address.setAddress("123 Main St");
        address.setCity("Springfield");
        address.setState("IL");
        address.setCountry("USA");
        address.setZipCode("62704");
        address.setDefault(true);
        address.setUpdated(now);
        address.setCreated(now);

        assertThat(address.getId()).isEqualTo("addr1");
        assertThat(address.getUser()).isEqualTo("user1");
        assertThat(address.getAddress()).isEqualTo("123 Main St");
        assertThat(address.getCity()).isEqualTo("Springfield");
        assertThat(address.getState()).isEqualTo("IL");
        assertThat(address.getCountry()).isEqualTo("USA");
        assertThat(address.getZipCode()).isEqualTo("62704");
        assertThat(address.isDefault()).isTrue();
        assertThat(address.getUpdated()).isEqualTo(now);
        assertThat(address.getCreated()).isEqualTo(now);
    }

    @Test
    void defaults() {
        Address address = new Address();
        assertThat(address.isDefault()).isFalse();
        assertThat(address.getCreated()).isNotNull();
    }
}
