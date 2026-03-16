package com.letsplay.productservice.repository;

import com.letsplay.productservice.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByUserId(String userId);
    List<Product> findByNameContainingIgnoreCase(String name);
    void deleteByUserId(String userId);

    // Filter by price range
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);

    // Search by name + price range
    @Query("{ 'name': { $regex: ?0, $options: 'i' }, 'price': { $gte: ?1, $lte: ?2 } }")
    List<Product> findByNameContainingIgnoreCaseAndPriceBetween(String name, Double minPrice, Double maxPrice);

    // Search by name or description keyword
    @Query("{ $or: [ { 'name': { $regex: ?0, $options: 'i' } }, { 'description': { $regex: ?0, $options: 'i' } } ] }")
    List<Product> searchByKeyword(String keyword);

    // Filter by price range only (min)
    List<Product> findByPriceGreaterThanEqual(Double minPrice);

    // Filter by price range only (max)
    List<Product> findByPriceLessThanEqual(Double maxPrice);
}
