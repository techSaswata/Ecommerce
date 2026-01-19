package com.ecommerce.config;

import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    
    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedUsers();
        }
        
        if (productRepository.count() == 0) {
            seedProducts();
        }
    }
    
    private void seedUsers() {
        log.info("Seeding users...");
        
        List<User> users = List.of(
                User.builder()
                        .username("john_doe")
                        .email("john@example.com")
                        .role("USER")
                        .build(),
                User.builder()
                        .username("jane_doe")
                        .email("jane@example.com")
                        .role("USER")
                        .build(),
                User.builder()
                        .username("admin")
                        .email("admin@example.com")
                        .role("ADMIN")
                        .build()
        );
        
        userRepository.saveAll(users);
        log.info("Created {} users", users.size());
    }
    
    private void seedProducts() {
        log.info("Seeding products...");
        
        List<Product> products = List.of(
                Product.builder()
                        .name("Laptop")
                        .description("Gaming Laptop with RTX 4060")
                        .price(50000.0)
                        .stock(10)
                        .category("Electronics")
                        .active(true)
                        .build(),
                Product.builder()
                        .name("Mouse")
                        .description("Wireless Gaming Mouse")
                        .price(1000.0)
                        .stock(50)
                        .category("Electronics")
                        .active(true)
                        .build(),
                Product.builder()
                        .name("Keyboard")
                        .description("Mechanical Gaming Keyboard")
                        .price(3000.0)
                        .stock(30)
                        .category("Electronics")
                        .active(true)
                        .build(),
                Product.builder()
                        .name("Headphones")
                        .description("Noise Cancelling Headphones")
                        .price(5000.0)
                        .stock(25)
                        .category("Electronics")
                        .active(true)
                        .build(),
                Product.builder()
                        .name("Monitor")
                        .description("27-inch 4K Gaming Monitor")
                        .price(25000.0)
                        .stock(15)
                        .category("Electronics")
                        .active(true)
                        .build()
        );
        
        productRepository.saveAll(products);
        log.info("Created {} products", products.size());
    }
}
