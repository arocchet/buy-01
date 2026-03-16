package com.letsplay.orderservice.repository;

import com.letsplay.orderservice.model.Order;
import com.letsplay.orderservice.model.OrderStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {

    List<Order> findByClientId(String clientId);

    List<Order> findByClientIdAndStatus(String clientId, OrderStatus status);

    // Find orders that contain a product sold by a given seller
    @Query("{ 'items.sellerId': ?0 }")
    List<Order> findByItemsSellerId(String sellerId);

    @Query("{ 'items.sellerId': ?0, 'status': ?1 }")
    List<Order> findByItemsSellerIdAndStatus(String sellerId, OrderStatus status);
}
