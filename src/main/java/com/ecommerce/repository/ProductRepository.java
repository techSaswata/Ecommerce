package com.ecommerce.repository;

import com.ecommerce.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    
    List<Product> findByActiveTrue();
    
    List<Product> findByCategory(String category);
    
    List<Product> findByCategoryAndActiveTrue(String category);
    
    @Query("{'name': {$regex: ?0, $options: 'i'}}")
    List<Product> searchByName(String name);
    
    @Query("{'$or': [{'name': {$regex: ?0, $options: 'i'}}, {'description': {$regex: ?0, $options: 'i'}}]}")
    List<Product> searchByNameOrDescription(String keyword);
    
    List<Product> findByStockGreaterThan(int stock);
    
    List<Product> findByPriceBetween(Double minPrice, Double maxPrice);
}
