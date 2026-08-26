package com.mernecommerce.identity.util;

import com.mernecommerce.identity.model.Brand;
import com.mernecommerce.identity.model.Merchant;
import com.mernecommerce.identity.model.User;

import java.util.Map;

// Builds the JSON shapes GET /api/user/me and friends return. Mongoose's
// `.populate()` replaces a reference id with the full referenced document
// (or a projected subset, for the {_id,name} case) — Spring Data has no
// direct equivalent, so these methods rebuild that shape manually from
// separately-fetched Merchant/Brand documents.
public final class UserMapper {

    // Full user document minus password, matching e.g. GET /api/user/me's
    // shape before the merchant populate is layered on top.
    public static Map<String, Object> toUserMap(User u) {
        return Json.of(
                "_id", u.getId(),
                "email", u.getEmail(),
                "phoneNumber", u.getPhoneNumber(),
                "firstName", u.getFirstName(),
                "lastName", u.getLastName(),
                "merchant", u.getMerchant(),
                "provider", u.getProvider(),
                "avatar", u.getAvatar(),
                "role", u.getRole(),
                "updated", u.getUpdated(),
                "created", u.getCreated()
        );
    }

    // {_id, name} only — matches `.populate('merchant', 'name')` used by
    // GET /api/user/search and GET /api/user (list).
    public static Map<String, Object> toMerchantNameRef(Merchant m) {
        return Json.of("_id", m.getId(), "name", m.getName());
    }

    // Full merchant document — matches the unrestricted
    // `.populate({path:'merchant', model:'Merchant', ...})` used by
    // GET /api/user/me.
    public static Map<String, Object> toMerchantMap(Merchant m) {
        return Json.of(
                "_id", m.getId(),
                "name", m.getName(),
                "email", m.getEmail(),
                "phoneNumber", m.getPhoneNumber(),
                "brandName", m.getBrandName(),
                "business", m.getBusiness(),
                "isActive", m.isActive(),
                "brand", m.getBrand(),
                "status", m.getStatus(),
                "updated", m.getUpdated(),
                "created", m.getCreated()
        );
    }

    // Full brand document — the innermost level of GET /api/user/me's
    // merchant -> brand nested populate.
    public static Map<String, Object> toBrandMap(Brand b) {
        return Json.of(
                "_id", b.getId(),
                "name", b.getName(),
                "slug", b.getSlug(),
                "description", b.getDescription(),
                "isActive", b.isActive(),
                "merchant", b.getMerchant(),
                "updated", b.getUpdated(),
                "created", b.getCreated()
        );
    }

    private UserMapper() {}
}
