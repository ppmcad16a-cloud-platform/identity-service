package com.mernecommerce.identity;

import com.mernecommerce.identity.repository.AddressRepository;
import com.mernecommerce.identity.repository.BrandRepository;
import com.mernecommerce.identity.repository.MerchantRepository;
import com.mernecommerce.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Boots the application with the REAL src/main/resources/application.properties
// (every other Spring test uses the test copy in src/test/resources, which
// shadows the main one on the classpath). Without this test, a broken
// production property - e.g. a circular placeholder or one that no longer
// exists in a new Spring version - only shows up when the container starts.
// Only the environment variables the file expects are supplied here, the
// same way Kubernetes supplies them.
@SpringBootTest(properties = {
        "MONGO_URI=mongodb://localhost:27017/config-test",
        // Not a real secret: just a 40-character placeholder (JwtService
        // requires at least 32 bytes).
        "JWT_SECRET=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
})
@TestPropertySource(locations = "file:src/main/resources/application.properties")
@AutoConfigureMockMvc
class ProductionConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private AddressRepository addressRepository;
    @MockitoBean
    private MerchantRepository merchantRepository;
    @MockitoBean
    private BrandRepository brandRepository;
    // See SecurityConfigTest for why deep stubs are needed here.
    @MockitoBean(answers = org.mockito.Answers.RETURNS_DEEP_STUBS)
    private MongoTemplate mongoTemplate;

    @Test
    void applicationStartsWithTheProductionConfiguration() throws Exception {
        mockMvc.perform(get("/health")).andExpect(status().isOk());
        // Routes are mounted under the default BASE_API_URL ("api").
        mockMvc.perform(get("/api/user/me")).andExpect(status().isUnauthorized());
    }
}
