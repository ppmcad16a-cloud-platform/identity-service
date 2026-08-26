package com.mernecommerce.identity.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

// A shipping/billing address owned by a User. Owned by identity_server —
// collection "addresses".
@Document(collection = "addresses")
public class Address {

    @Id
    @JsonProperty("_id")
    private String id;

    private String user;
    private String address;
    private String city;
    private String state;
    private String country;
    private String zipCode;
    private boolean isDefault = false;
    private Instant updated;
    private Instant created = Instant.now();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    // Jackson infers "default" (not "isDefault") as the JSON property name
    // from a bare isDefault()/setDefault() bean pair (it strips the "is"
    // prefix per JavaBean convention) — @JsonProperty pins it back to
    // "isDefault" to match Mongoose's field name, which client/ reads
    // directly (e.g. Address/actions.js).
    @JsonProperty("isDefault")
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public Instant getUpdated() { return updated; }
    public void setUpdated(Instant updated) { this.updated = updated; }

    public Instant getCreated() { return created; }
    public void setCreated(Instant created) { this.created = created; }
}
