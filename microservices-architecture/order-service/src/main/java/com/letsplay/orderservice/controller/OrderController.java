package com.letsplay.orderservice.controller;

import com.letsplay.orderservice.dto.CheckoutRequest;
import com.letsplay.orderservice.dto.UpdateOrderStatusRequest;
import com.letsplay.orderservice.model.Order;
import com.letsplay.orderservice.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ── Checkout ─────────────────────────────────────────────────────────────

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(@Valid @RequestBody CheckoutRequest request, Authentication auth) {
        Order order = orderService.checkout(auth.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    // ── Client ────────────────────────────────────────────────────────────────

    @GetMapping("/my")
    public ResponseEntity<List<Order>> getMyOrders(
            @RequestParam(required = false) String search,
            Authentication auth) {
        return ResponseEntity.ok(orderService.getClientOrders(auth.getName(), search));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable String id, Authentication auth) {
        return ResponseEntity.ok(orderService.cancelOrder(auth.getName(), id));
    }

    @GetMapping("/my/stats")
    public ResponseEntity<Map<String, Object>> getClientStats(Authentication auth) {
        return ResponseEntity.ok(orderService.getClientStats(auth.getName()));
    }

    // ── Seller ────────────────────────────────────────────────────────────────

    @GetMapping("/seller")
    public ResponseEntity<List<Order>> getSellerOrders(
            @RequestParam(required = false) String search,
            Authentication auth) {
        requireRole(auth, "SELLER");
        return ResponseEntity.ok(orderService.getSellerOrders(auth.getName(), search));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(
            @PathVariable String id,
            @RequestBody UpdateOrderStatusRequest request,
            Authentication auth) {
        requireRole(auth, "SELLER");
        return ResponseEntity.ok(orderService.updateOrderStatus(auth.getName(), id, request));
    }

    @GetMapping("/seller/stats")
    public ResponseEntity<Map<String, Object>> getSellerStats(Authentication auth) {
        requireRole(auth, "SELLER");
        return ResponseEntity.ok(orderService.getSellerStats(auth.getName()));
    }

    // ── Detail ────────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable String id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void requireRole(Authentication auth, String role) {
        boolean hasRole = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_" + role));
        if (!hasRole) {
            throw new com.letsplay.orderservice.exception.BadRequestException("Access denied: requires role " + role);
        }
    }
}
