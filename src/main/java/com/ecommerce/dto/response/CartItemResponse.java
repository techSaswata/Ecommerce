package com.ecommerce.dto.response;

import com.ecommerce.model.CartItem;
import com.ecommerce.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    
    private String id;
    private String productId;
    private Integer quantity;
    private ProductInfo product;
    private Double subtotal;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private String id;
        private String name;
        private Double price;
        private String imageUrl;
        private Integer availableStock;
    }
    
    public static CartItemResponse fromCartItemAndProduct(CartItem cartItem, Product product) {
        ProductInfo productInfo = ProductInfo.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .imageUrl(product.getImageUrl())
                .availableStock(product.getStock())
                .build();
        
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .productId(cartItem.getProductId())
                .quantity(cartItem.getQuantity())
                .product(productInfo)
                .subtotal(product.getPrice() * cartItem.getQuantity())
                .build();
    }
}
