package com.ecommerce.webhook;

import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.service.PaymentService;
import com.razorpay.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Webhook endpoints for payment callbacks")
public class PaymentWebhookController {
    
    private final PaymentService paymentService;
    
    @Value("${razorpay.webhook-secret}")
    private String webhookSecret;
    
    @PostMapping("/payment")
    @Operation(summary = "Receive payment webhook from Razorpay")
    public ResponseEntity<ApiResponse<String>> handlePaymentWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        
        log.info("Received payment webhook");
        log.debug("Webhook payload: {}", payload);
        
        try {
            // Verify webhook signature
            boolean isValid = Utils.verifyWebhookSignature(payload, signature, webhookSecret);
            
            if (!isValid) {
                log.error("Invalid webhook signature");
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid signature"));
            }
            
            // Parse webhook payload
            JSONObject webhookData = new JSONObject(payload);
            String event = webhookData.getString("event");
            
            log.info("Processing webhook event: {}", event);
            
            JSONObject payloadData = webhookData.getJSONObject("payload");
            JSONObject payment = payloadData.getJSONObject("payment").getJSONObject("entity");
            
            String razorpayPaymentId = payment.getString("id");
            String razorpayOrderId = payment.getString("order_id");
            
            switch (event) {
                case "payment.captured":
                    log.info("Payment captured: {}", razorpayPaymentId);
                    paymentService.handlePaymentCapture(razorpayPaymentId, razorpayOrderId);
                    break;
                    
                case "payment.authorized":
                    log.info("Payment authorized: {}", razorpayPaymentId);
                    // Optionally handle authorization - can auto-capture or wait
                    break;
                    
                case "payment.failed":
                    log.info("Payment failed: {}", razorpayPaymentId);
                    String errorDescription = payment.optString("error_description", "Payment failed");
                    paymentService.handlePaymentFailure(razorpayOrderId, errorDescription);
                    break;
                    
                default:
                    log.info("Unhandled webhook event: {}", event);
            }
            
            return ResponseEntity.ok(ApiResponse.success("Webhook processed successfully"));
            
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Webhook processing failed: " + e.getMessage()));
        }
    }
    
    /**
     * Mock webhook endpoint for testing without Razorpay
     * This simulates a successful payment callback
     */
    @PostMapping("/payment/mock")
    @Operation(summary = "Mock payment webhook for testing")
    public ResponseEntity<ApiResponse<String>> handleMockPaymentWebhook(
            @RequestBody MockWebhookRequest request) {
        
        log.info("Received mock payment webhook for order: {}", request.getRazorpayOrderId());
        
        try {
            if ("success".equalsIgnoreCase(request.getStatus())) {
                paymentService.handlePaymentCapture(
                        "pay_mock_" + System.currentTimeMillis(),
                        request.getRazorpayOrderId()
                );
                return ResponseEntity.ok(ApiResponse.success("Mock payment captured successfully"));
            } else {
                paymentService.handlePaymentFailure(
                        request.getRazorpayOrderId(),
                        request.getFailureReason() != null ? request.getFailureReason() : "Mock payment failed"
                );
                return ResponseEntity.ok(ApiResponse.success("Mock payment failure processed"));
            }
            
        } catch (Exception e) {
            log.error("Error processing mock webhook: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Mock webhook processing failed: " + e.getMessage()));
        }
    }
    
    @lombok.Data
    public static class MockWebhookRequest {
        private String razorpayOrderId;
        private String status; // "success" or "failed"
        private String failureReason;
    }
}
