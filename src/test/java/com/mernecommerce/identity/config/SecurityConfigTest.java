package com.mernecommerce.identity.config;

import com.mernecommerce.identity.repository.AddressRepository;
import com.mernecommerce.identity.repository.BrandRepository;
import com.mernecommerce.identity.repository.MerchantRepository;
import com.mernecommerce.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// End-to-end (real Spring context, real HTTP dispatch via MockMvc) coverage
// of SecurityConfig's actual routing rules — the per-route permitAll/
// authenticated split can't be verified by unit-testing the controllers
// directly (that bypasses Spring Security entirely), and this is also what
// exercises SecurityConfig's @Bean factory methods themselves. Every
// repository/MongoTemplate is @MockBean'd — no real MongoDB connection is
// made.
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private AddressRepository addressRepository;
    @MockBean
    private MerchantRepository merchantRepository;
    @MockBean
    private BrandRepository brandRepository;
    // Spring Boot's auto-configured GridFsTemplate bean (unused by this app,
    // but still wired up by MongoDataAutoConfiguration) calls
    // mongoTemplate.getConverter() while *building the context itself* —
    // before any @BeforeEach runs. A plain @MockBean returns null there,
    // which blows up context startup with "MongoConverter must not be
    // null". RETURNS_DEEP_STUBS makes every unstubbed method (including
    // getConverter()) return a further mock instead of null.
    @MockBean(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
    private MongoTemplate mongoTemplate;

    @Test
    void protectedUserRoutesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/user")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/user/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/user/search").param("search", "x")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/api/user").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedAddressAddAndListRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/address/add").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/address")).andExpect(status().isUnauthorized());
    }

    @Test
    void addressByIdRoutesArePublicByDesign() throws Exception {
        when(addressRepository.findById("a1")).thenReturn(Optional.empty());
        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(com.mernecommerce.identity.model.Address.class)))
                .thenReturn(null);
        when(addressRepository.existsById("a1")).thenReturn(false);

        mockMvc.perform(get("/api/address/a1")).andExpect(status().isNotFound());
        mockMvc.perform(put("/api/address/a1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/address/delete/a1")).andExpect(status().isOk());
    }

    @Test
    void authLoginAndForgotAndTokenResetArePublic() throws Exception {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());
        when(userRepository.findByResetPasswordTokenAndResetPasswordExpiresAfter(eq("tok"), any()))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@example.com\",\"password\":\"secret1\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/auth/forgot").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody@example.com\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/auth/reset/tok").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"newpass1\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void authResetSelfRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/auth/reset").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"old1\",\"confirmPassword\":\"new1\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unmappedRouteUnderApiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/does-not-exist")).andExpect(status().isUnauthorized());
    }
}
