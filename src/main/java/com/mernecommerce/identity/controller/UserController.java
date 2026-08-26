package com.mernecommerce.identity.controller;

import com.mernecommerce.identity.constants.Roles;
import com.mernecommerce.identity.model.Merchant;
import com.mernecommerce.identity.model.User;
import com.mernecommerce.identity.repository.BrandRepository;
import com.mernecommerce.identity.repository.MerchantRepository;
import com.mernecommerce.identity.repository.UserRepository;
import com.mernecommerce.identity.util.Json;
import com.mernecommerce.identity.util.UserMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Pattern;

// Port of routes/api/user.js. GET /search and GET / share the same
// {password:0,_id:0} projection quirk as the original (yes, the id really
// is excluded there too — preserved for parity, see the Node route's own
// comment). PUT / deviates from the original on purpose: the Node version
// applies `req.body.profile` onto the user document with no field
// whitelist, which lets a caller set `role`/`merchant` on themselves via
// this endpoint — a real privilege-escalation bug. This port restricts the
// update to the fields the client's own Account form actually edits
// (firstName, lastName, phoneNumber, avatar) and no longer echoes the
// password hash back in the response, both flagged to the user rather than
// silently ported forward.
@RestController
@RequestMapping("/${BASE_API_URL:api}/user")
public class UserController {

    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final BrandRepository brandRepository;
    private final MongoTemplate mongoTemplate;

    public UserController(UserRepository userRepository, MerchantRepository merchantRepository,
                           BrandRepository brandRepository, MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.merchantRepository = merchantRepository;
        this.brandRepository = brandRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@AuthenticationPrincipal User principal, @RequestParam("search") String search) {
        try {
            ResponseEntity<?> forbidden = requireAdmin(principal);
            if (forbidden != null) return forbidden;

            String regex = Pattern.quote(search);
            Criteria criteria = new Criteria().orOperator(
                    Criteria.where("firstName").regex(regex, "i"),
                    Criteria.where("lastName").regex(regex, "i"),
                    Criteria.where("email").regex(regex, "i")
            );
            Query query = new Query(criteria);
            query.fields().exclude("password").exclude("_id");

            List<User> users = mongoTemplate.find(query, User.class, "users");
            List<Map<String, Object>> result = users.stream().map(this::toSearchListItem).toList();

            return ResponseEntity.ok(Json.of("users", result));
        } catch (Exception e) {
            return genericError();
        }
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int limit) {
        try {
            Query query = new Query();
            query.fields().exclude("password").exclude("_id");
            query.with(Sort.by(Sort.Direction.DESC, "created"));
            query.with(PageRequest.of(Math.max(page - 1, 0), limit));

            List<User> users = mongoTemplate.find(query, User.class, "users");
            List<Map<String, Object>> result = users.stream().map(this::toSearchListItem).toList();

            long count = mongoTemplate.count(new Query(), User.class, "users");

            return ResponseEntity.ok(Json.of(
                    "users", result,
                    "totalPages", (long) Math.ceil((double) count / limit),
                    "currentPage", page,
                    "count", count
            ));
        } catch (Exception e) {
            return genericError();
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User principal) {
        try {
            Optional<User> userOpt = userRepository.findById(principal.getId());
            if (userOpt.isEmpty()) {
                return genericError();
            }
            User user = userOpt.get();

            Map<String, Object> userMap = UserMapper.toUserMap(user);
            userMap.put("merchant", resolveMerchantWithBrand(user.getMerchant()));

            return ResponseEntity.ok(Json.of("user", userMap));
        } catch (Exception e) {
            return genericError();
        }
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal User principal,
                                            @RequestBody Map<String, Object> body) {
        try {
            Object profileObj = body.get("profile");
            Map<?, ?> profile = profileObj instanceof Map ? (Map<?, ?>) profileObj : Map.of();

            Optional<User> userOpt = userRepository.findById(principal.getId());
            if (userOpt.isEmpty()) {
                return genericError();
            }
            User user = userOpt.get();

            // Whitelist — see class-level note.
            if (profile.get("firstName") instanceof String v) user.setFirstName(v);
            if (profile.get("lastName") instanceof String v) user.setLastName(v);
            if (profile.get("phoneNumber") instanceof String v) user.setPhoneNumber(v);
            if (profile.get("avatar") instanceof String v) user.setAvatar(v);

            User saved = userRepository.save(user);

            return ResponseEntity.ok(Json.of(
                    "success", true,
                    "message", "Your profile is successfully updated!",
                    "user", UserMapper.toUserMap(saved)
            ));
        } catch (Exception e) {
            return genericError();
        }
    }

    private Map<String, Object> resolveMerchantWithBrand(String merchantId) {
        if (merchantId == null) return null;
        Optional<Merchant> merchantOpt = merchantRepository.findById(merchantId);
        if (merchantOpt.isEmpty()) return null;
        Merchant merchant = merchantOpt.get();
        Map<String, Object> merchantMap = UserMapper.toMerchantMap(merchant);
        if (merchant.getBrand() != null) {
            brandRepository.findById(merchant.getBrand())
                    .ifPresent(brand -> merchantMap.put("brand", UserMapper.toBrandMap(brand)));
        }
        return merchantMap;
    }

    // GET /search and GET / both project out password AND _id (see class
    // note), then populate `merchant` down to just {_id, name}.
    private Map<String, Object> toSearchListItem(User user) {
        Map<String, Object> map = Json.of(
                "email", user.getEmail(),
                "phoneNumber", user.getPhoneNumber(),
                "firstName", user.getFirstName(),
                "lastName", user.getLastName(),
                "provider", user.getProvider(),
                "avatar", user.getAvatar(),
                "role", user.getRole(),
                "updated", user.getUpdated(),
                "created", user.getCreated()
        );
        if (user.getMerchant() != null) {
            map.put("merchant", merchantRepository.findById(user.getMerchant())
                    .map(UserMapper::toMerchantNameRef)
                    .orElse(null));
        } else {
            map.put("merchant", null);
        }
        return map;
    }

    private static ResponseEntity<?> requireAdmin(User principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        if (!Roles.ADMIN.equals(principal.getRole())) {
            return ResponseEntity.status(403).body("You are not allowed to make this request.");
        }
        return null;
    }

    private static ResponseEntity<?> genericError() {
        return ResponseEntity.status(400).body(Json.of("error", "Your request could not be processed. Please try again."));
    }
}
