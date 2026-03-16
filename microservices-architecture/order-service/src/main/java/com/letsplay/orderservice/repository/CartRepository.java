package com.letsplay.orderservice.repository;

import com.letsplay.orderservice.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends MongoRepository<Cart, String> {
    // findFirst avoids "non unique result" if duplicate carts exist
    Optional<Cart> findFirstByUserId(String userId);
    List<Cart> findAllByUserId(String userId);
    void deleteByUserId(String userId);
}
