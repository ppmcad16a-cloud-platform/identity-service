package com.mernecommerce.identity.controller;

import com.mernecommerce.identity.model.Address;
import com.mernecommerce.identity.model.User;
import com.mernecommerce.identity.repository.AddressRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
class AddressControllerTest {

    private final AddressRepository addressRepository = mock(AddressRepository.class);
    private final MongoTemplate mongoTemplate = mock(MongoTemplate.class);
    private final AddressController controller = new AddressController(addressRepository, mongoTemplate);

    private static User principal(String id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    // ---- add ----

    @Test
    void addSuccessWithAllFields() {
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        Map<String, Object> body = new HashMap<>();
        body.put("address", "123 Main St");
        body.put("city", "Springfield");
        body.put("state", "IL");
        body.put("country", "USA");
        body.put("zipCode", "62704");
        body.put("isDefault", true);

        ResponseEntity<?> res = controller.add(principal("u1"), body);

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        Map<?, ?> resBody = (Map<?, ?>) res.getBody();
        Address saved = (Address) resBody.get("address");
        assertThat(saved.getAddress()).isEqualTo("123 Main St");
        assertThat(saved.getCity()).isEqualTo("Springfield");
        assertThat(saved.getState()).isEqualTo("IL");
        assertThat(saved.getCountry()).isEqualTo("USA");
        assertThat(saved.getZipCode()).isEqualTo("62704");
        assertThat(saved.isDefault()).isTrue();
        assertThat(saved.getUser()).isEqualTo("u1");
    }

    @Test
    void addSuccessWithNoOptionalFields() {
        when(addressRepository.save(any(Address.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> res = controller.add(principal("u1"), Map.of());

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        Map<?, ?> resBody = (Map<?, ?>) res.getBody();
        Address saved = (Address) resBody.get("address");
        assertThat(saved.getAddress()).isNull();
        assertThat(saved.isDefault()).isFalse();
    }

    @Test
    void addHandlesUnexpectedException() {
        when(addressRepository.save(any(Address.class))).thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> res = controller.add(principal("u1"), Map.of());

        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }

    // ---- list ----

    @Test
    void listSuccess() {
        Address a = new Address();
        a.setId("a1");
        when(addressRepository.findByUser("u1")).thenReturn(List.of(a));

        ResponseEntity<?> res = controller.list(principal("u1"));

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        assertThat(((Map<?, ?>) res.getBody()).get("addresses")).isEqualTo(List.of(a));
    }

    @Test
    void listHandlesUnexpectedException() {
        when(addressRepository.findByUser("u1")).thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> res = controller.list(principal("u1"));

        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }

    // ---- get ----

    @Test
    void getFound() {
        Address a = new Address();
        a.setId("a1");
        when(addressRepository.findById("a1")).thenReturn(Optional.of(a));

        ResponseEntity<?> res = controller.get("a1");

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        assertThat(((Map<?, ?>) res.getBody()).get("address")).isEqualTo(a);
    }

    @Test
    void getNotFound() {
        when(addressRepository.findById("missing")).thenReturn(Optional.empty());

        ResponseEntity<?> res = controller.get("missing");

        assertThat(res.getStatusCode().value()).isEqualTo(404);
        assertThat(((Map<?, ?>) res.getBody()).get("message"))
                .isEqualTo("Cannot find Address with the id: missing.");
    }

    @Test
    void getHandlesUnexpectedException() {
        when(addressRepository.findById("a1")).thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> res = controller.get("a1");

        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }

    // ---- update ----

    @Test
    void updateSkipsIdAndUnderscoreIdKeysButAppliesOthers() {
        Map<String, Object> body = new HashMap<>();
        body.put("_id", "should-be-skipped");
        body.put("id", "should-also-be-skipped");
        body.put("city", "NewCity");

        ResponseEntity<?> res = controller.update("a1", body);

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        verify(mongoTemplate).updateFirst(any(Query.class), any(Update.class), eq(Address.class));
    }

    @Test
    void updateWithOnlyIdKeyStillSucceeds() {
        Map<String, Object> body = new HashMap<>();
        body.put("id", "should-be-skipped");

        ResponseEntity<?> res = controller.update("a1", body);

        assertThat(res.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void updateHandlesUnexpectedException() {
        when(mongoTemplate.updateFirst(any(Query.class), any(Update.class), eq(Address.class)))
                .thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> res = controller.update("a1", Map.of("city", "X"));

        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }

    // ---- delete ----

    @Test
    void deleteExistingAddress() {
        when(addressRepository.existsById("a1")).thenReturn(true);

        ResponseEntity<?> res = controller.delete("a1");

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        Map<?, ?> addressBody = (Map<?, ?>) ((Map<?, ?>) res.getBody()).get("address");
        assertThat(addressBody.get("deletedCount")).isEqualTo(1);
        verify(addressRepository).deleteById("a1");
    }

    @Test
    void deleteNonExistentAddress() {
        when(addressRepository.existsById("missing")).thenReturn(false);

        ResponseEntity<?> res = controller.delete("missing");

        assertThat(res.getStatusCode().value()).isEqualTo(200);
        Map<?, ?> addressBody = (Map<?, ?>) ((Map<?, ?>) res.getBody()).get("address");
        assertThat(addressBody.get("deletedCount")).isEqualTo(0);
    }

    @Test
    void deleteHandlesUnexpectedException() {
        when(addressRepository.existsById("a1")).thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> res = controller.delete("a1");

        assertThat(res.getStatusCode().value()).isEqualTo(400);
    }
}
