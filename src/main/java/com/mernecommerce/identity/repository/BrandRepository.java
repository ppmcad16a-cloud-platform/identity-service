package com.mernecommerce.identity.repository;

import com.mernecommerce.identity.model.Brand;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BrandRepository extends MongoRepository<Brand, String> {
}
