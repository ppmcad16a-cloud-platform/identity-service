package com.mernecommerce.identity.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MerchantTest {

    @Test
    void gettersAndSettersRoundTrip() {
        Merchant merchant = new Merchant();
        Instant now = Instant.now();

        merchant.setId("m1");
        merchant.setName("Acme");
        merchant.setEmail("acme@example.com");
        merchant.setPhoneNumber("555-0000");
        merchant.setBrandName("AcmeBrand");
        merchant.setBusiness("Widgets");
        merchant.setActive(true);
        merchant.setBrand("brand1");
        merchant.setStatus("Approved");
        merchant.setUpdated(now);
        merchant.setCreated(now);

        assertThat(merchant.getId()).isEqualTo("m1");
        assertThat(merchant.getName()).isEqualTo("Acme");
        assertThat(merchant.getEmail()).isEqualTo("acme@example.com");
        assertThat(merchant.getPhoneNumber()).isEqualTo("555-0000");
        assertThat(merchant.getBrandName()).isEqualTo("AcmeBrand");
        assertThat(merchant.getBusiness()).isEqualTo("Widgets");
        assertThat(merchant.isActive()).isTrue();
        assertThat(merchant.getBrand()).isEqualTo("brand1");
        assertThat(merchant.getStatus()).isEqualTo("Approved");
        assertThat(merchant.getUpdated()).isEqualTo(now);
        assertThat(merchant.getCreated()).isEqualTo(now);
    }

    @Test
    void defaults() {
        Merchant merchant = new Merchant();
        assertThat(merchant.isActive()).isFalse();
    }
}
