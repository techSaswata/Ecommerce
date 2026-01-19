package com.ecommerce.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
    
    private String name;
    
    private String description;
    
    @Positive(message = "Price must be positive")
    private Double price;
    
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;
    
    private String category;
    
    private String imageUrl;
    
    private Boolean active;
}
