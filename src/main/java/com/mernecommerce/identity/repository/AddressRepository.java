package com.mernecommerce.identity.repository;

import com.mernecommerce.identity.model.Address;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AddressRepository extends MongoRepository<Address, String> {
    List<Address> findByUser(String userId);
}
