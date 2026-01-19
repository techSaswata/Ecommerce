package com.ecommerce.controller;

import com.ecommerce.dto.request.AddToCartRequest;
import com.ecommerce.dto.request.UpdateCartRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.CartItemResponse;
import com.ecommerce.dto.response.CartResponse;
import com.ecommerce.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Shopping Cart", description = "APIs for managing shopping cart")
public class CartController {
    
    private final CartService cartService;
    
    @PostMapping("/add")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartItemResponse>> addToCart(
            @Valid @RequestBody AddToCartRequest request) {
        CartItemResponse item = cartService.addToCart(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to cart", item));
    }
    
    @GetMapping("/{userId}")
    @Operation(summary = "Get user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
            @PathVariable String userId) {
        CartResponse cart = cartService.getCart(userId);
        return ResponseEntity.ok(ApiResponse.success(cart));
    }
    
    @PutMapping("/{userId}/items/{productId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateCartItem(
            @PathVariable String userId,
            @PathVariable String productId,
            @Valid @RequestBody UpdateCartRequest request) {
        CartItemResponse item = cartService.updateCartItem(userId, productId, request);
        return ResponseEntity.ok(ApiResponse.success("Cart item updated", item));
    }
    
    @DeleteMapping("/{userId}/items/{productId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(
            @PathVariable String userId,
            @PathVariable String productId) {
        cartService.removeFromCart(userId, productId);
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart"));
    }
    
    @DeleteMapping("/{userId}/clear")
    @Operation(summary = "Clear user's cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @PathVariable String userId) {
        cartService.clearCart(userId);
        return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully"));
    }
}
