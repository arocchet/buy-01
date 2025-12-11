package com.letsplay.letsplay.repository;

import com.letsplay.letsplay.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByUserId(String userId);
    List<Product> findByNameContainingIgnoreCase(String name);
}