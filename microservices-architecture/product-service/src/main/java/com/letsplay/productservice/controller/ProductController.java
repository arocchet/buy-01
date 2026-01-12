package com.letsplay.productservice.controller;

import com.letsplay.productservice.dto.ProductRequest;
import com.letsplay.productservice.model.Product;
import com.letsplay.productservice.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Product>> getProductsByUser(@PathVariable String userId) {
        return ResponseEntity.ok(productService.getProductsByUserId(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String name) {
        return ResponseEntity.ok(productService.searchProductsByName(name));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {
        String userId = getUserIdFromAuth(authentication);
        String role = getRoleFromAuth(authentication);
        Product product = productService.createProduct(request, userId, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody ProductRequest request,
            Authentication authentication) {
        String userId = getUserIdFromAuth(authentication);
        String role = getRoleFromAuth(authentication);
        Product product = productService.updateProduct(id, request, userId, role);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable String id,
            Authentication authentication) {
        String userId = getUserIdFromAuth(authentication);
        String role = getRoleFromAuth(authentication);
        productService.deleteProduct(id, userId, role);
        return ResponseEntity.noContent().build();
    }

    private String getUserIdFromAuth(Authentication authentication) {
        // The user ID is stored in the authentication name (from JWT subject)
        return authentication.getName();
    }

    private String getRoleFromAuth(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.replace("ROLE_", "").toLowerCase())
                .findFirst()
                .orElse("client");
    }
}
