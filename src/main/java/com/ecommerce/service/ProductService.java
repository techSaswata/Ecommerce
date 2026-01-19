package com.ecommerce.service;

import com.ecommerce.dto.request.CreateProductRequest;
import com.ecommerce.dto.request.UpdateProductRequest;
import com.ecommerce.dto.response.ProductResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Product;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Creating product: {}", request.getName());
        
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .active(true)
                .build();
        
        Product savedProduct = productRepository.save(product);
        log.info("Product created with ID: {}", savedProduct.getId());
        
        return ProductResponse.fromProduct(savedProduct);
    }
    
    public ProductResponse getProductById(String productId) {
        Product product = findProductById(productId);
        return ProductResponse.fromProduct(product);
    }
    
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductResponse::fromProduct)
                .collect(Collectors.toList());
    }
    
    public List<ProductResponse> getActiveProducts() {
        return productRepository.findByActiveTrue().stream()
                .map(ProductResponse::fromProduct)
                .collect(Collectors.toList());
    }
    
    public List<ProductResponse> getProductsByCategory(String category) {
        return productRepository.findByCategoryAndActiveTrue(category).stream()
                .map(ProductResponse::fromProduct)
                .collect(Collectors.toList());
    }
    
    public List<ProductResponse> searchProducts(String keyword) {
        return productRepository.searchByNameOrDescription(keyword).stream()
                .map(ProductResponse::fromProduct)
                .collect(Collectors.toList());
    }
    
    public ProductResponse updateProduct(String productId, UpdateProductRequest request) {
        log.info("Updating product: {}", productId);
        
        Product product = findProductById(productId);
        
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }
        if (request.getCategory() != null) {
            product.setCategory(request.getCategory());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        
        Product updatedProduct = productRepository.save(product);
        log.info("Product updated: {}", productId);
        
        return ProductResponse.fromProduct(updatedProduct);
    }
    
    public void deleteProduct(String productId) {
        log.info("Deleting product: {}", productId);
        
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }
        
        productRepository.deleteById(productId);
        log.info("Product deleted: {}", productId);
    }
    
    public Product findProductById(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }
    
    public void updateStock(String productId, int quantityChange) {
        Product product = findProductById(productId);
        int newStock = product.getStock() + quantityChange;
        product.setStock(Math.max(0, newStock));
        productRepository.save(product);
        log.info("Updated stock for product {}: {} -> {}", productId, product.getStock() - quantityChange, newStock);
    }
    
    public boolean hasEnoughStock(String productId, int requiredQuantity) {
        Product product = findProductById(productId);
        return product.getStock() >= requiredQuantity;
    }
}
