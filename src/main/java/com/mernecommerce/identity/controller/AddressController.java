package com.mernecommerce.identity.controller;

import com.mernecommerce.identity.model.Address;
import com.mernecommerce.identity.model.User;
import com.mernecommerce.identity.repository.AddressRepository;
import com.mernecommerce.identity.util.Json;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.data.mongodb.core.query.Criteria.where;

// Port of routes/api/address.js. NOTE, preserved verbatim from the
// original: GET/PUT by {id} and DELETE /delete/{id} have no auth
// requirement (see SecurityConfig's permitAll rules for these three) — any
// caller who knows/guesses an address id can read, edit, or delete it. This
// is an existing quirk being carried over faithfully, not something
// introduced by this port; flagged here the same way the original Node
// route flagged it in a comment.
@RestController
@RequestMapping("/${BASE_API_URL:api}/address")
public class AddressController {

    private final AddressRepository addressRepository;
    private final MongoTemplate mongoTemplate;

    public AddressController(AddressRepository addressRepository, MongoTemplate mongoTemplate) {
        this.addressRepository = addressRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @PostMapping("/add")
    public ResponseEntity<?> add(@AuthenticationPrincipal User principal, @RequestBody Map<String, Object> body) {
        try {
            Address address = new Address();
            applyFields(address, body);
            address.setUser(principal.getId());

            Address saved = addressRepository.save(address);

            return ResponseEntity.ok(Json.of(
                    "success", true,
                    "message", "Address has been added successfully!",
                    "address", saved
            ));
        } catch (Exception e) {
            return genericError();
        }
    }

    @GetMapping
    public ResponseEntity<?> list(@AuthenticationPrincipal User principal) {
        try {
            List<Address> addresses = addressRepository.findByUser(principal.getId());
            return ResponseEntity.ok(Json.of("addresses", addresses));
        } catch (Exception e) {
            return genericError();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable String id) {
        try {
            Optional<Address> addressOpt = addressRepository.findById(id);
            if (addressOpt.isEmpty()) {
                return ResponseEntity.status(404).body(Json.of(
                        "message", "Cannot find Address with the id: " + id + "."));
            }
            return ResponseEntity.ok(Json.of("address", addressOpt.get()));
        } catch (Exception e) {
            return genericError();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        try {
            Update update = new Update();
            for (Map.Entry<String, Object> entry : body.entrySet()) {
                if ("_id".equals(entry.getKey()) || "id".equals(entry.getKey())) continue;
                update.set(entry.getKey(), entry.getValue());
            }
            mongoTemplate.updateFirst(new Query(where("_id").is(id)), update, Address.class);

            return ResponseEntity.ok(Json.of(
                    "success", true,
                    "message", "Address has been updated successfully!"
            ));
        } catch (Exception e) {
            return genericError();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        try {
            boolean existed = addressRepository.existsById(id);
            addressRepository.deleteById(id);

            return ResponseEntity.ok(Json.of(
                    "success", true,
                    "message", "Address has been deleted successfully!",
                    "address", Json.of("deletedCount", existed ? 1 : 0)
            ));
        } catch (Exception e) {
            return genericError();
        }
    }

    private static void applyFields(Address address, Map<String, Object> body) {
        if (body.get("address") instanceof String v) address.setAddress(v);
        if (body.get("city") instanceof String v) address.setCity(v);
        if (body.get("state") instanceof String v) address.setState(v);
        if (body.get("country") instanceof String v) address.setCountry(v);
        if (body.get("zipCode") instanceof String v) address.setZipCode(v);
        if (body.get("isDefault") instanceof Boolean v) address.setDefault(v);
    }

    private static ResponseEntity<?> genericError() {
        return ResponseEntity.status(400).body(Json.of("error", "Your request could not be processed. Please try again."));
    }
}
