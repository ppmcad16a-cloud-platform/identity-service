package com.mernecommerce.identity.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

// Trimmed, populate-only copy of catalog_server's Brand model — needed for
// the nested populate in GET /api/user/me (merchant -> brand). Never
// written to from here. Same shared MongoDB database, not a data copy.
@Document(collection = "brands")
public class Brand {

    @Id
    @JsonProperty("_id")
    private String id;

    private String name;
    private String slug;
    private String description;
    private boolean isActive = true;
    private String merchant;
    private Instant updated;
    private Instant created;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getMerchant() { return merchant; }
    public void setMerchant(String merchant) { this.merchant = merchant; }

    public Instant getUpdated() { return updated; }
    public void setUpdated(Instant updated) { this.updated = updated; }

    public Instant getCreated() { return created; }
    public void setCreated(Instant created) { this.created = created; }
}
