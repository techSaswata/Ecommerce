package com.ecommerce.service;

import com.ecommerce.dto.request.CreateOrderRequest;
import com.ecommerce.dto.response.OrderResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.Product;
import com.ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final ProductService productService;
    
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        log.info("Creating order for user: {}", request.getUserId());
        
        // Validate cart and stock
        cartService.validateCartForCheckout(request.getUserId());
        
        // Get cart items
        List<CartItem> cartItems = cartService.getCartItems(request.getUserId());
        
        // Build order items and calculate total
        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0;
        
        for (CartItem cartItem : cartItems) {
            Product product = productService.findProductById(cartItem.getProductId());
            
            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .subtotal(product.getPrice() * cartItem.getQuantity())
                    .build();
            
            orderItems.add(orderItem);
            totalAmount += orderItem.getSubtotal();
            
            // Reduce stock
            productService.updateStock(product.getId(), -cartItem.getQuantity());
        }
        
        // Create order
        Order order = Order.builder()
                .userId(request.getUserId())
                .totalAmount(totalAmount)
                .status(Order.OrderStatus.CREATED)
                .items(orderItems)
                .shippingAddress(request.getShippingAddress())
                .build();
        
        Order savedOrder = orderRepository.save(order);
        log.info("Order created with ID: {}", savedOrder.getId());
        
        // Clear cart after successful order creation
        cartService.clearCart(request.getUserId());
        
        return OrderResponse.fromOrder(savedOrder);
    }
    
    public OrderResponse getOrderById(String orderId) {
        Order order = findOrderById(orderId);
        return OrderResponse.fromOrder(order);
    }
    
    public List<OrderResponse> getOrdersByUserId(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
    }
    
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::fromOrder)
                .collect(Collectors.toList());
    }
    
    public Order findOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
    }
    
    public Order findOrderByRazorpayOrderId(String razorpayOrderId) {
        return orderRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "razorpayOrderId", razorpayOrderId));
    }
    
    @Transactional
    public OrderResponse updateOrderStatus(String orderId, Order.OrderStatus status) {
        log.info("Updating order {} status to: {}", orderId, status);
        
        Order order = findOrderById(orderId);
        order.setStatus(status);
        Order savedOrder = orderRepository.save(order);
        
        log.info("Order status updated successfully");
        return OrderResponse.fromOrder(savedOrder);
    }
    
    @Transactional
    public OrderResponse updateRazorpayOrderId(String orderId, String razorpayOrderId) {
        log.info("Updating order {} with Razorpay Order ID: {}", orderId, razorpayOrderId);
        
        Order order = findOrderById(orderId);
        order.setRazorpayOrderId(razorpayOrderId);
        order.setStatus(Order.OrderStatus.PENDING_PAYMENT);
        Order savedOrder = orderRepository.save(order);
        
        return OrderResponse.fromOrder(savedOrder);
    }
    
    @Transactional
    public OrderResponse cancelOrder(String orderId) {
        log.info("Cancelling order: {}", orderId);
        
        Order order = findOrderById(orderId);
        
        // Can only cancel if not paid
        if (order.getStatus() == Order.OrderStatus.PAID || 
            order.getStatus() == Order.OrderStatus.PROCESSING ||
            order.getStatus() == Order.OrderStatus.SHIPPED ||
            order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel order with status: " + order.getStatus());
        }
        
        // Restore stock
        for (OrderItem item : order.getItems()) {
            productService.updateStock(item.getProductId(), item.getQuantity());
        }
        
        order.setStatus(Order.OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        
        log.info("Order cancelled successfully");
        return OrderResponse.fromOrder(savedOrder);
    }
    
    @Transactional
    public void markOrderAsPaid(String orderId) {
        log.info("Marking order as paid: {}", orderId);
        Order order = findOrderById(orderId);
        order.setStatus(Order.OrderStatus.PAID);
        orderRepository.save(order);
    }
    
    @Transactional
    public void markOrderAsFailed(String orderId) {
        log.info("Marking order as failed: {}", orderId);
        Order order = findOrderById(orderId);
        order.setStatus(Order.OrderStatus.FAILED);
        
        // Restore stock on payment failure
        for (OrderItem item : order.getItems()) {
            productService.updateStock(item.getProductId(), item.getQuantity());
        }
        
        orderRepository.save(order);
    }
}
