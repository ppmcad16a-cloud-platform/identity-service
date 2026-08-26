package com.mernecommerce.identity.controller;

import com.mernecommerce.identity.model.Brand;
import com.mernecommerce.identity.model.Merchant;
import com.mernecommerce.identity.model.User;
import com.mernecommerce.identity.repository.BrandRepository;
import com.mernecommerce.identity.repository.MerchantRepository;
import com.mernecommerce.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class UserControllerTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final MerchantRepository merchantRepository = mock(MerchantRepository.class);
    private final BrandRepository brandRepository = mock(BrandRepository.class);
    private final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
    private final UserController controller =
            new UserController(userRepository, merchantRepository, brandRepository, mongoTemplate);

    private static User user(String id, String role, String merchantId) {
        User u = new User();
        u.setId(id);
        u.setEmail(id + "@example.com");
        u.setFirstName("First");
        u.setLastName("Last");
        u.setRole(role);
        u.setMerchant(merchantId);
        return u;
    }

    // ---- search ----

    @Test
    void searchNoPrincipalReturns401() {
        ResponseEntity<?> res = controller.search(null, "foo");
        assertThat(res.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void searchNonAdminReturns403() {
        ResponseEntity<?> res = controller.search(user("u1", "ROLE MEMBER", null), "foo");
        assertThat(res.getStatusCode().value()).isEqualTo(403);
    }

    @Test
    void searchAdminSuccessWithMerchantVariants() {
        User noMerchant = user("u1", "ROLE MEMBER", null);
        User merchantFound = user("u2", "ROLE MERCHANT", "m1");
        User merchantMissing = user("u3", "ROLE MERCHANT", "m-missing");

        when(mongoTemplate.find(any(Query.class), eq(User.class), eq("users")))
                .thenReturn(List.of(noMerchant, merchantFound, merchantMissing));

        Merchant merchant = new Merchant();
        merchant.setId("m1");
        merchant.setName("Acme");
        when(merchantRepository.findById("m1")).thenReturn(Optional.of(merchant));
        when(merchantRepository.findById("m-missing")).thenReturn(Optional.empty());

        ResponseEntity<?> res = controller.search(user("admin", "ROLE ADMIN", null), "foo");

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        List<Map<String, Object>> users = (List<Map<String, Object>>) ((Map<String, Object>) res.getBody()).get("users");
        assertThat(users).hasSize(3);
        assertThat(users.get(0).get("merchant")).isNull();
        assertThat(users.get(1).get("merchant")).isEqualTo(Map.of("_id", "m1", "name", "Acme"));
        assertThat(users.get(2).get("merchant")).isNull();
    }

    @Test
    void searchHandlesUnexpectedException() {
        when(mongoTemplate.find(any(Query.class), eq(User.class), eq("users")))
                .thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> res = controller.search(user("admin", "ROLE ADMIN", null), "foo");

        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }

    // ---- list ----

    @Test
    void listSuccess() {
        User plain = user("u1", "ROLE MEMBER", null);
        when(mongoTemplate.find(any(Query.class), eq(User.class), eq("users"))).thenReturn(List.of(plain));
        when(mongoTemplate.count(any(Query.class), eq(User.class), eq("users"))).thenReturn(1L);

        ResponseEntity<?> res = controller.list(1, 10);

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertThat(body.get("totalPages")).isEqualTo(1L);
        assertThat(body.get("currentPage")).isEqualTo(1);
        assertThat(body.get("count")).isEqualTo(1L);
    }

    @Test
    void listHandlesUnexpectedException() {
        when(mongoTemplate.find(any(Query.class), eq(User.class), eq("users")))
                .thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> res = controller.list(1, 10);

        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }

    // ---- me ----

    @Test
    void meUserNotFound() {
        when(userRepository.findById("u1")).thenReturn(Optional.empty());

        ResponseEntity<?> res = controller.me(user("u1", "ROLE MEMBER", null));

        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void meWithNoMerchant() {
        User u = user("u1", "ROLE MEMBER", null);
        when(userRepository.findById("u1")).thenReturn(Optional.of(u));

        ResponseEntity<?> res = controller.me(u);

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        Map<String, Object> userMap = (Map<String, Object>) ((Map<String, Object>) res.getBody()).get("user");
        assertThat(userMap.get("merchant")).isNull();
    }

    @Test
    void meWithMerchantNotFound() {
        User u = user("u1", "ROLE MERCHANT", "m-missing");
        when(userRepository.findById("u1")).thenReturn(Optional.of(u));
        when(merchantRepository.findById("m-missing")).thenReturn(Optional.empty());

        ResponseEntity<?> res = controller.me(u);

        Map<String, Object> userMap = (Map<String, Object>) ((Map<String, Object>) res.getBody()).get("user");
        assertThat(userMap.get("merchant")).isNull();
    }

    @Test
    void meWithMerchantAndNoBrand() {
        User u = user("u1", "ROLE MERCHANT", "m1");
        when(userRepository.findById("u1")).thenReturn(Optional.of(u));
        Merchant merchant = new Merchant();
        merchant.setId("m1");
        merchant.setBrand(null);
        when(merchantRepository.findById("m1")).thenReturn(Optional.of(merchant));

        ResponseEntity<?> res = controller.me(u);

        Map<String, Object> userMap = (Map<String, Object>) ((Map<String, Object>) res.getBody()).get("user");
        Map<String, Object> merchantMap = (Map<String, Object>) userMap.get("merchant");
        assertThat(merchantMap).doesNotContainKey("brand-resolved");
        assertThat(merchantMap.get("brand")).isNull();
    }

    @Test
    void meWithMerchantAndBrandMissing() {
        User u = user("u1", "ROLE MERCHANT", "m1");
        when(userRepository.findById("u1")).thenReturn(Optional.of(u));
        Merchant merchant = new Merchant();
        merchant.setId("m1");
        merchant.setBrand("b-missing");
        when(merchantRepository.findById("m1")).thenReturn(Optional.of(merchant));
        when(brandRepository.findById("b-missing")).thenReturn(Optional.empty());

        ResponseEntity<?> res = controller.me(u);

        assertThat(res.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void meWithMerchantAndBrandFound() {
        User u = user("u1", "ROLE MERCHANT", "m1");
        when(userRepository.findById("u1")).thenReturn(Optional.of(u));
        Merchant merchant = new Merchant();
        merchant.setId("m1");
        merchant.setBrand("b1");
        when(merchantRepository.findById("m1")).thenReturn(Optional.of(merchant));
        Brand brand = new Brand();
        brand.setId("b1");
        brand.setName("Nike");
        when(brandRepository.findById("b1")).thenReturn(Optional.of(brand));

        ResponseEntity<?> res = controller.me(u);

        Map<String, Object> userMap = (Map<String, Object>) ((Map<String, Object>) res.getBody()).get("user");
        Map<String, Object> merchantMap = (Map<String, Object>) userMap.get("merchant");
        Map<String, Object> brandMap = (Map<String, Object>) merchantMap.get("brand");
        assertThat(brandMap.get("_id")).isEqualTo("b1");
        assertThat(brandMap.get("name")).isEqualTo("Nike");
    }

    @Test
    void meHandlesUnexpectedException() {
        when(userRepository.findById("u1")).thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> res = controller.me(user("u1", "ROLE MEMBER", null));

        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }

    // ---- updateProfile ----

    @Test
    void updateProfileWithWhitelistedFields() {
        User u = user("u1", "ROLE MEMBER", null);
        when(userRepository.findById("u1")).thenReturn(Optional.of(u));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> profile = new HashMap<>();
        profile.put("firstName", "NewFirst");
        profile.put("lastName", "NewLast");
        profile.put("phoneNumber", "555-9999");
        profile.put("avatar", "new-avatar.png");
        profile.put("role", "ROLE ADMIN"); // not whitelisted, must be ignored

        ResponseEntity<?> res = controller.updateProfile(u, Map.of("profile", profile));

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        assertThat(u.getFirstName()).isEqualTo("NewFirst");
        assertThat(u.getLastName()).isEqualTo("NewLast");
        assertThat(u.getPhoneNumber()).isEqualTo("555-9999");
        assertThat(u.getAvatar()).isEqualTo("new-avatar.png");
        assertThat(u.getRole()).isEqualTo("ROLE MEMBER");
    }

    @Test
    void updateProfileWithMissingProfileKeyAppliesNoFields() {
        User u = user("u1", "ROLE MEMBER", null);
        when(userRepository.findById("u1")).thenReturn(Optional.of(u));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> res = controller.updateProfile(u, Map.of());

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        assertThat(u.getFirstName()).isEqualTo("First");
    }

    @Test
    void updateProfileWithNonMapProfileValueAppliesNoFields() {
        User u = user("u1", "ROLE MEMBER", null);
        when(userRepository.findById("u1")).thenReturn(Optional.of(u));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> res = controller.updateProfile(u, Map.of("profile", "not-a-map"));

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        assertThat(u.getFirstName()).isEqualTo("First");
    }

    @Test
    void updateProfileUserNotFound() {
        when(userRepository.findById("u1")).thenReturn(Optional.empty());

        ResponseEntity<?> res = controller.updateProfile(user("u1", "ROLE MEMBER", null), Map.of());

        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void updateProfileHandlesUnexpectedException() {
        when(userRepository.findById("u1")).thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> res = controller.updateProfile(user("u1", "ROLE MEMBER", null), Map.of());

        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }
}
