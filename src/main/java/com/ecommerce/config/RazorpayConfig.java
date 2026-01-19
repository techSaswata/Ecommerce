package com.ecommerce.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RazorpayConfig {
    
    @Value("${razorpay.key-id}")
    private String keyId;
    
    @Value("${razorpay.key-secret}")
    private String keySecret;
    
    /**
     * Creates a Razorpay client bean for dependency injection.
     * Note: This bean may fail if invalid credentials are provided.
     * The PaymentService handles client creation internally for better error handling.
     */
    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        log.info("Initializing Razorpay client with key: {}", keyId.substring(0, Math.min(10, keyId.length())) + "...");
        return new RazorpayClient(keyId, keySecret);
    }
}
