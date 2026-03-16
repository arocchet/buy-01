package com.letsplay.orderservice.controller;

import com.letsplay.orderservice.dto.CartItemRequest;
import com.letsplay.orderservice.model.Cart;
import com.letsplay.orderservice.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<Cart> getCart(Authentication auth) {
        return ResponseEntity.ok(cartService.getOrCreateCart(auth.getName()));
    }

    @PostMapping("/items")
    public ResponseEntity<Cart> addItem(@Valid @RequestBody CartItemRequest request, Authentication auth) {
        return ResponseEntity.ok(cartService.addItem(auth.getName(), request));
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<Cart> updateItem(@PathVariable String productId,
                                           @RequestBody Map<String, Integer> body,
                                           Authentication auth) {
        int quantity = body.getOrDefault("quantity", 1);
        return ResponseEntity.ok(cartService.updateItemQuantity(auth.getName(), productId, quantity));
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Cart> removeItem(@PathVariable String productId, Authentication auth) {
        return ResponseEntity.ok(cartService.removeItem(auth.getName(), productId));
    }

    @DeleteMapping
    public ResponseEntity<Cart> clearCart(Authentication auth) {
        return ResponseEntity.ok(cartService.clearCart(auth.getName()));
    }
}
