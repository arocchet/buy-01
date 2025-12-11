package com.letsplay.letsplay.controller;

import com.letsplay.letsplay.dto.ProductRequest;
import com.letsplay.letsplay.model.Product;
import com.letsplay.letsplay.model.User;
import com.letsplay.letsplay.service.ProductService;
import com.letsplay.letsplay.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable String id) {
        try {
            Product product = getProductWithSecurityCheck(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving product: " + e.getMessage());
        }
    }

    @PostAuthorize("hasRole('ADMIN') or returnObject.userId == authentication.name")
    private Product getProductWithSecurityCheck(String id) {
        Optional<Product> product = productService.getProductById(id);
        if (product.isEmpty()) {
            throw new RuntimeException("Product not found");
        }
        return product.get();
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createProduct(@Valid @RequestBody ProductRequest productRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            Optional<User> userOptional = userService.getUserByEmail(userEmail);
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            Product product = new Product();
            product.setName(productRequest.getName());
            product.setDescription(productRequest.getDescription());
            product.setPrice(productRequest.getPrice());
            product.setUserId(userOptional.get().getId());

            Product result = productService.createProduct(product);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error creating product: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateProduct(@PathVariable String id, @Valid @RequestBody ProductRequest productRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            Optional<User> userOptional = userService.getUserByEmail(userEmail);
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            User user = userOptional.get();

            if (!user.getRole().equals("admin") && !productService.isProductOwner(id, user.getId())) {
                return ResponseEntity.status(403).body("Access denied: You can only modify your own products");
            }

            Product productDetails = new Product();
            productDetails.setName(productRequest.getName());
            productDetails.setDescription(productRequest.getDescription());
            productDetails.setPrice(productRequest.getPrice());

            Product updatedProduct = productService.updateProduct(id, productDetails);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteProduct(@PathVariable String id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            Optional<User> userOptional = userService.getUserByEmail(userEmail);
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }

            User user = userOptional.get();

            if (!user.getRole().equals("admin") && !productService.isProductOwner(id, user.getId())) {
                return ResponseEntity.status(403).body("Access denied: You can only delete your own products");
            }

            productService.deleteProduct(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Product>> getProductsByUserId(@PathVariable String userId) {
        List<Product> products = productService.getProductsByUserId(userId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String name) {
        List<Product> products = productService.searchProductsByName(name);
        return ResponseEntity.ok(products);
    }
}