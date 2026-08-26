package com.mernecommerce.identity.repository;

import com.mernecommerce.identity.model.Merchant;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MerchantRepository extends MongoRepository<Merchant, String> {
}
