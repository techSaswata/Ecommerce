package com.ecommerce.service;

import com.ecommerce.dto.request.AddToCartRequest;
import com.ecommerce.dto.request.UpdateCartRequest;
import com.ecommerce.dto.response.CartItemResponse;
import com.ecommerce.dto.response.CartResponse;
import com.ecommerce.exception.BadRequestException;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import com.ecommerce.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {
    
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;
    
    @Transactional
    public CartItemResponse addToCart(AddToCartRequest request) {
        log.info("Adding to cart - User: {}, Product: {}, Quantity: {}", 
                request.getUserId(), request.getProductId(), request.getQuantity());
        
        // Validate product exists
        Product product = productService.findProductById(request.getProductId());
        
        // Check stock availability
        if (product.getStock() < request.getQuantity()) {
            throw new InsufficientStockException(product.getName(), request.getQuantity(), product.getStock());
        }
        
        // Check if item already exists in cart
        Optional<CartItem> existingItem = cartItemRepository
                .findByUserIdAndProductId(request.getUserId(), request.getProductId());
        
        CartItem cartItem;
        if (existingItem.isPresent()) {
            // Update quantity
            cartItem = existingItem.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            
            // Validate total quantity against stock
            if (product.getStock() < newQuantity) {
                throw new InsufficientStockException(product.getName(), newQuantity, product.getStock());
            }
            
            cartItem.setQuantity(newQuantity);
            log.info("Updated cart item quantity to: {}", newQuantity);
        } else {
            // Create new cart item
            cartItem = CartItem.builder()
                    .userId(request.getUserId())
                    .productId(request.getProductId())
                    .quantity(request.getQuantity())
                    .build();
            log.info("Created new cart item");
        }
        
        CartItem savedItem = cartItemRepository.save(cartItem);
        return CartItemResponse.fromCartItemAndProduct(savedItem, product);
    }
    
    public CartResponse getCart(String userId) {
        log.info("Getting cart for user: {}", userId);
        
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);
        List<CartItemResponse> itemResponses = new ArrayList<>();
        double totalAmount = 0;
        int totalItems = 0;
        
        for (CartItem item : cartItems) {
            try {
                Product product = productService.findProductById(item.getProductId());
                CartItemResponse response = CartItemResponse.fromCartItemAndProduct(item, product);
                itemResponses.add(response);
                totalAmount += response.getSubtotal();
                totalItems += item.getQuantity();
            } catch (ResourceNotFoundException e) {
                // Product was deleted, remove from cart
                log.warn("Product {} not found, removing from cart", item.getProductId());
                cartItemRepository.delete(item);
            }
        }
        
        return CartResponse.builder()
                .userId(userId)
                .items(itemResponses)
                .totalItems(totalItems)
                .totalAmount(totalAmount)
                .build();
    }
    
    public CartItemResponse updateCartItem(String userId, String productId, UpdateCartRequest request) {
        log.info("Updating cart item - User: {}, Product: {}, New Quantity: {}", 
                userId, productId, request.getQuantity());
        
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        
        Product product = productService.findProductById(productId);
        
        // Validate stock
        if (product.getStock() < request.getQuantity()) {
            throw new InsufficientStockException(product.getName(), request.getQuantity(), product.getStock());
        }
        
        cartItem.setQuantity(request.getQuantity());
        CartItem savedItem = cartItemRepository.save(cartItem);
        
        return CartItemResponse.fromCartItemAndProduct(savedItem, product);
    }
    
    @Transactional
    public void removeFromCart(String userId, String productId) {
        log.info("Removing from cart - User: {}, Product: {}", userId, productId);
        
        if (!cartItemRepository.findByUserIdAndProductId(userId, productId).isPresent()) {
            throw new ResourceNotFoundException("Cart item not found");
        }
        
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
        log.info("Cart item removed");
    }
    
    @Transactional
    public void clearCart(String userId) {
        log.info("Clearing cart for user: {}", userId);
        cartItemRepository.deleteByUserId(userId);
        log.info("Cart cleared for user: {}", userId);
    }
    
    public List<CartItem> getCartItems(String userId) {
        return cartItemRepository.findByUserId(userId);
    }
    
    public boolean isCartEmpty(String userId) {
        return cartItemRepository.countByUserId(userId) == 0;
    }
    
    public void validateCartForCheckout(String userId) {
        List<CartItem> items = cartItemRepository.findByUserId(userId);
        
        if (items.isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }
        
        for (CartItem item : items) {
            Product product = productService.findProductById(item.getProductId());
            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException(
                        product.getName(), 
                        item.getQuantity(), 
                        product.getStock()
                );
            }
        }
    }
}
