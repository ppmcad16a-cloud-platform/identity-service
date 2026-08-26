package com.mernecommerce.identity.constants;

// Mirrors constants/index.js's ROLES across every microservice in this
// project — the exact string values are stored in MongoDB and shared
// between Java and Node services, so they must match byte-for-byte.
public final class Roles {
    public static final String ADMIN = "ROLE ADMIN";
    public static final String MEMBER = "ROLE MEMBER";
    public static final String MERCHANT = "ROLE MERCHANT";

    private Roles() {}
}
