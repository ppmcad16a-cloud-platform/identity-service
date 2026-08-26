package com.mernecommerce.identity.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

// Trimmed, populate-only copy of commerce_server's Merchant model —
// identity_server only needs this registered to resolve the User ->
// merchant -> brand populate chain in GET /api/user/me and the merchant
// name shown on GET /api/user (list/search). Never written to from here.
// Same shared MongoDB database as every other service, not a data copy.
@Document(collection = "merchants")
public class Merchant {

    @Id
    @JsonProperty("_id")
    private String id;

    private String name;
    private String email;
    private String phoneNumber;
    private String brandName;
    private String business;
    private boolean isActive = false;
    private String brand;
    private String status;
    private Instant updated;
    private Instant created;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }

    public String getBusiness() { return business; }
    public void setBusiness(String business) { this.business = business; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Instant getUpdated() { return updated; }
    public void setUpdated(Instant updated) { this.updated = updated; }

    public Instant getCreated() { return created; }
    public void setCreated(Instant created) { this.created = created; }
}
