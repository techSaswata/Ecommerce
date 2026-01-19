package com.ecommerce.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {
    
    @Id
    private String id;
    
    @Indexed
    private String userId;
    
    private Double totalAmount;
    
    @Indexed
    private OrderStatus status;
    
    private List<OrderItem> items;
    
    private String shippingAddress;
    
    private String razorpayOrderId;
    
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
    
    public enum OrderStatus {
        CREATED,
        PENDING_PAYMENT,
        PAID,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        FAILED
    }
}
