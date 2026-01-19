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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String orderId;
    
    private Double amount;
    
    private String currency;
    
    @Indexed
    private PaymentStatus status;
    
    @Indexed
    private String razorpayOrderId;
    
    @Indexed
    private String razorpayPaymentId;
    
    private String razorpaySignature;
    
    private String paymentMethod;
    
    private String failureReason;
    
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
    
    public enum PaymentStatus {
        PENDING,
        AUTHORIZED,
        CAPTURED,
        SUCCESS,
        FAILED,
        REFUNDED
    }
}
