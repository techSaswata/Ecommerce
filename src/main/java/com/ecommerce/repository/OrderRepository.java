package com.ecommerce.repository;

import com.ecommerce.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    
    List<Order> findByUserId(String userId);
    
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<Order> findByStatus(Order.OrderStatus status);
    
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);
    
    List<Order> findByUserIdAndStatus(String userId, Order.OrderStatus status);
    
    List<Order> findByCreatedAtBetween(Instant start, Instant end);
}
