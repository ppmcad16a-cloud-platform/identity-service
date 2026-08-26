package com.mernecommerce.identity.util;

import com.mernecommerce.identity.model.Brand;
import com.mernecommerce.identity.model.Merchant;
import com.mernecommerce.identity.model.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void toUserMapIncludesAllFieldsExceptPassword() {
        User user = new User();
        user.setId("u1");
        user.setEmail("a@b.com");
        user.setPhoneNumber("555");
        user.setFirstName("First");
        user.setLastName("Last");
        user.setPassword("secret-hash");
        user.setMerchant("m1");
        user.setProvider("Email");
        user.setAvatar("avatar.png");
        user.setRole("ROLE MEMBER");
        Instant now = Instant.now();
        user.setUpdated(now);
        user.setCreated(now);

        Map<String, Object> map = UserMapper.toUserMap(user);

        assertThat(map).doesNotContainKey("password");
        assertThat(map.get("_id")).isEqualTo("u1");
        assertThat(map.get("email")).isEqualTo("a@b.com");
        assertThat(map.get("phoneNumber")).isEqualTo("555");
        assertThat(map.get("firstName")).isEqualTo("First");
        assertThat(map.get("lastName")).isEqualTo("Last");
        assertThat(map.get("merchant")).isEqualTo("m1");
        assertThat(map.get("provider")).isEqualTo("Email");
        assertThat(map.get("avatar")).isEqualTo("avatar.png");
        assertThat(map.get("role")).isEqualTo("ROLE MEMBER");
        assertThat(map.get("updated")).isEqualTo(now);
        assertThat(map.get("created")).isEqualTo(now);
    }

    @Test
    void toMerchantNameRefIncludesOnlyIdAndName() {
        Merchant merchant = new Merchant();
        merchant.setId("m1");
        merchant.setName("Acme");
        merchant.setEmail("should-not-appear@example.com");

        Map<String, Object> map = UserMapper.toMerchantNameRef(merchant);

        assertThat(map).containsExactly(
                Map.entry("_id", "m1"),
                Map.entry("name", "Acme")
        );
    }

    @Test
    void toMerchantMapIncludesEveryField() {
        Merchant merchant = new Merchant();
        merchant.setId("m1");
        merchant.setName("Acme");
        merchant.setEmail("acme@example.com");
        merchant.setPhoneNumber("555");
        merchant.setBrandName("AcmeBrand");
        merchant.setBusiness("Widgets");
        merchant.setActive(true);
        merchant.setBrand("b1");
        merchant.setStatus("Approved");
        Instant now = Instant.now();
        merchant.setUpdated(now);
        merchant.setCreated(now);

        Map<String, Object> map = UserMapper.toMerchantMap(merchant);

        assertThat(map.get("_id")).isEqualTo("m1");
        assertThat(map.get("name")).isEqualTo("Acme");
        assertThat(map.get("email")).isEqualTo("acme@example.com");
        assertThat(map.get("phoneNumber")).isEqualTo("555");
        assertThat(map.get("brandName")).isEqualTo("AcmeBrand");
        assertThat(map.get("business")).isEqualTo("Widgets");
        assertThat(map.get("isActive")).isEqualTo(true);
        assertThat(map.get("brand")).isEqualTo("b1");
        assertThat(map.get("status")).isEqualTo("Approved");
        assertThat(map.get("updated")).isEqualTo(now);
        assertThat(map.get("created")).isEqualTo(now);
    }

    @Test
    void toBrandMapIncludesEveryField() {
        Brand brand = new Brand();
        brand.setId("b1");
        brand.setName("Nike");
        brand.setSlug("nike");
        brand.setDescription("Sportswear");
        brand.setActive(false);
        brand.setMerchant("m1");
        Instant now = Instant.now();
        brand.setUpdated(now);
        brand.setCreated(now);

        Map<String, Object> map = UserMapper.toBrandMap(brand);

        assertThat(map.get("_id")).isEqualTo("b1");
        assertThat(map.get("name")).isEqualTo("Nike");
        assertThat(map.get("slug")).isEqualTo("nike");
        assertThat(map.get("description")).isEqualTo("Sportswear");
        assertThat(map.get("isActive")).isEqualTo(false);
        assertThat(map.get("merchant")).isEqualTo("m1");
        assertThat(map.get("updated")).isEqualTo(now);
        assertThat(map.get("created")).isEqualTo(now);
    }

    @Test
    void privateConstructorIsNotInstantiablePublicly() throws Exception {
        Constructor<UserMapper> constructor = UserMapper.class.getDeclaredConstructor();
        assertThat(Modifier.isPrivate(constructor.getModifiers())).isTrue();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
