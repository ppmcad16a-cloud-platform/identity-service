package com.mernecommerce.identity.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class BrandTest {

    @Test
    void gettersAndSettersRoundTrip() {
        Brand brand = new Brand();
        Instant now = Instant.now();

        brand.setId("b1");
        brand.setName("Nike");
        brand.setSlug("nike");
        brand.setDescription("Sportswear");
        brand.setActive(false);
        brand.setMerchant("merchant1");
        brand.setUpdated(now);
        brand.setCreated(now);

        assertThat(brand.getId()).isEqualTo("b1");
        assertThat(brand.getName()).isEqualTo("Nike");
        assertThat(brand.getSlug()).isEqualTo("nike");
        assertThat(brand.getDescription()).isEqualTo("Sportswear");
        assertThat(brand.isActive()).isFalse();
        assertThat(brand.getMerchant()).isEqualTo("merchant1");
        assertThat(brand.getUpdated()).isEqualTo(now);
        assertThat(brand.getCreated()).isEqualTo(now);
    }

    @Test
    void defaults() {
        Brand brand = new Brand();
        assertThat(brand.isActive()).isTrue();
    }
}
