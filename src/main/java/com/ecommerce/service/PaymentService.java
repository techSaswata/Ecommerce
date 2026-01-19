package com.ecommerce.service;

import com.ecommerce.dto.request.CreatePaymentRequest;
import com.ecommerce.dto.request.VerifyPaymentRequest;
import com.ecommerce.dto.response.PaymentResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.PaymentException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Order;
import com.ecommerce.model.Payment;
import com.ecommerce.repository.PaymentRepository;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    
    @Value("${razorpay.key-id}")
    private String razorpayKeyId;
    
    @Value("${razorpay.key-secret}")
    private String razorpayKeySecret;
    
    @Value("${razorpay.webhook-secret}")
    private String webhookSecret;
    
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        log.info("Creating payment for order: {}", request.getOrderId());
        
        // Validate order exists and is in correct status
        Order order = orderService.findOrderById(request.getOrderId());
        
        if (order.getStatus() != Order.OrderStatus.CREATED) {
            throw new BadRequestException("Order is not in CREATED status. Current status: " + order.getStatus());
        }
        
        // Check if payment already exists
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(request.getOrderId());
        if (existingPayment.isPresent() && 
            existingPayment.get().getStatus() != Payment.PaymentStatus.FAILED) {
            throw new BadRequestException("Payment already exists for this order");
        }
        
        String currency = request.getCurrency() != null ? request.getCurrency() : "INR";
        
        try {
            // Create Razorpay client
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            
            // Create Razorpay order
            JSONObject orderRequest = new JSONObject();
            // Razorpay expects amount in paise (smallest currency unit)
            orderRequest.put("amount", (int) (request.getAmount() * 100));
            orderRequest.put("currency", currency);
            orderRequest.put("receipt", "order_" + request.getOrderId());
            
            com.razorpay.Order razorpayOrder = razorpay.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");
            
            log.info("Razorpay order created: {}", razorpayOrderId);
            
            // Create payment record
            Payment payment = Payment.builder()
                    .orderId(request.getOrderId())
                    .amount(request.getAmount())
                    .currency(currency)
                    .status(Payment.PaymentStatus.PENDING)
                    .razorpayOrderId(razorpayOrderId)
                    .build();
            
            Payment savedPayment = paymentRepository.save(payment);
            
            // Update order with Razorpay order ID
            orderService.updateRazorpayOrderId(request.getOrderId(), razorpayOrderId);
            
            log.info("Payment created with ID: {}", savedPayment.getId());
            
            return PaymentResponse.fromPayment(savedPayment);
            
        } catch (RazorpayException e) {
            log.error("Razorpay error: {}", e.getMessage());
            throw new PaymentException("Failed to create payment: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    public PaymentResponse verifyPayment(VerifyPaymentRequest request) {
        log.info("Verifying payment - Order: {}, Payment: {}", 
                request.getRazorpayOrderId(), request.getRazorpayPaymentId());
        
        // Find payment by Razorpay order ID
        Payment payment = paymentRepository.findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "razorpayOrderId", request.getRazorpayOrderId()));
        
        try {
            // Verify signature
            String generatedSignature = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
            boolean isValid = Utils.verifySignature(generatedSignature, request.getRazorpaySignature(), razorpayKeySecret);
            
            if (!isValid) {
                log.error("Payment signature verification failed");
                payment.setStatus(Payment.PaymentStatus.FAILED);
                payment.setFailureReason("Signature verification failed");
                paymentRepository.save(payment);
                
                orderService.markOrderAsFailed(payment.getOrderId());
                
                throw new PaymentException("Payment signature verification failed");
            }
            
            // Update payment
            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
            payment.setRazorpaySignature(request.getRazorpaySignature());
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            Payment savedPayment = paymentRepository.save(payment);
            
            // Update order status
            orderService.markOrderAsPaid(payment.getOrderId());
            
            log.info("Payment verified successfully");
            
            return PaymentResponse.fromPayment(savedPayment);
            
        } catch (RazorpayException e) {
            log.error("Error verifying payment: {}", e.getMessage());
            throw new PaymentException("Failed to verify payment: " + e.getMessage(), e);
        }
    }
    
    public PaymentResponse getPaymentByOrderId(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));
        return PaymentResponse.fromPayment(payment);
    }
    
    public PaymentResponse getPaymentById(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));
        return PaymentResponse.fromPayment(payment);
    }
    
    public Payment findPaymentByRazorpayOrderId(String razorpayOrderId) {
        return paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "razorpayOrderId", razorpayOrderId));
    }
    
    @Transactional
    public void handlePaymentCapture(String razorpayPaymentId, String razorpayOrderId) {
        log.info("Handling payment capture - Payment: {}, Order: {}", razorpayPaymentId, razorpayOrderId);
        
        Payment payment = findPaymentByRazorpayOrderId(razorpayOrderId);
        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setStatus(Payment.PaymentStatus.CAPTURED);
        paymentRepository.save(payment);
        
        orderService.markOrderAsPaid(payment.getOrderId());
        
        log.info("Payment captured successfully");
    }
    
    @Transactional
    public void handlePaymentFailure(String razorpayOrderId, String reason) {
        log.info("Handling payment failure - Order: {}, Reason: {}", razorpayOrderId, reason);
        
        Payment payment = findPaymentByRazorpayOrderId(razorpayOrderId);
        payment.setStatus(Payment.PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        paymentRepository.save(payment);
        
        orderService.markOrderAsFailed(payment.getOrderId());
        
        log.info("Payment failure handled");
    }
    
    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }
}
