package com.letsplay.orderservice.service;

import com.letsplay.orderservice.dto.CheckoutRequest;
import com.letsplay.orderservice.dto.UpdateOrderStatusRequest;
import com.letsplay.orderservice.exception.BadRequestException;
import com.letsplay.orderservice.exception.ResourceNotFoundException;
import com.letsplay.orderservice.model.*;
import com.letsplay.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
    }

    // ── Client operations ────────────────────────────────────────────────────

    /** Checkout: convert the current cart into an Order */
    public Order checkout(String clientId, CheckoutRequest request) {
        Cart cart = cartService.getOrCreateCart(clientId);

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Order order = new Order();
        order.setClientId(clientId);
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "PAY_ON_DELIVERY");
        order.setStatus(OrderStatus.PENDING);

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(cartItem -> new OrderItem(
                        cartItem.getProductId(),
                        cartItem.getProductName(),
                        cartItem.getQuantity(),
                        cartItem.getUnitPrice(),
                        cartItem.getSellerId()))
                .collect(Collectors.toList());

        order.setItems(orderItems);
        order.recalculateTotal();

        Order savedOrder = orderRepository.save(order);

        // Clear cart after checkout
        cartService.clearCart(clientId);

        return savedOrder;
    }

    /** Get all orders for a client, optionally filtered by status keyword */
    public List<Order> getClientOrders(String clientId, String search) {
        List<Order> orders = orderRepository.findByClientId(clientId);
        if (search != null && !search.isBlank()) {
            String lower = search.toLowerCase();
            orders = orders.stream()
                    .filter(o -> o.getStatus().name().toLowerCase().contains(lower)
                            || o.getItems().stream().anyMatch(i -> i.getProductName().toLowerCase().contains(lower)))
                    .collect(Collectors.toList());
        }
        return orders;
    }

    /** Cancel an order (client can only cancel PENDING orders) */
    public Order cancelOrder(String clientId, String orderId) {
        Order order = getOrderById(orderId);
        if (!order.getClientId().equals(clientId)) {
            throw new BadRequestException("You can only cancel your own orders");
        }
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Cannot cancel an order with status: " + order.getStatus());
        }
        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    // ── Seller operations ────────────────────────────────────────────────────

    /** Get orders containing products sold by this seller, optionally filtered */
    public List<Order> getSellerOrders(String sellerId, String search) {
        List<Order> orders = orderRepository.findByItemsSellerId(sellerId);
        if (search != null && !search.isBlank()) {
            String lower = search.toLowerCase();
            orders = orders.stream()
                    .filter(o -> o.getStatus().name().toLowerCase().contains(lower)
                            || o.getItems().stream().anyMatch(i ->
                                    i.getSellerId().equals(sellerId) &&
                                    i.getProductName().toLowerCase().contains(lower)))
                    .collect(Collectors.toList());
        }
        return orders;
    }

    /** Seller updates order status (confirm, ship, deliver) */
    public Order updateOrderStatus(String sellerId, String orderId, UpdateOrderStatusRequest request) {
        Order order = getOrderById(orderId);

        boolean sellerHasItems = order.getItems().stream()
                .anyMatch(i -> i.getSellerId().equals(sellerId));
        if (!sellerHasItems) {
            throw new BadRequestException("You don't have items in this order");
        }
        if (request.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Use the cancel endpoint to cancel an order");
        }

        order.setStatus(request.getStatus());
        return orderRepository.save(order);
    }

    // ── Profile statistics ────────────────────────────────────────────────────

    /** Stats for a client: total spent, most bought products */
    public Map<String, Object> getClientStats(String clientId) {
        List<Order> orders = orderRepository.findByClientId(clientId);

        double totalSpent = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .mapToDouble(o -> o.getTotalAmount() != null ? o.getTotalAmount() : 0)
                .sum();

        // Count quantity per product
        Map<String, Long> productCount = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .flatMap(o -> o.getItems().stream())
                .collect(Collectors.groupingBy(OrderItem::getProductId,
                        Collectors.summingLong(OrderItem::getQuantity)));

        List<Map<String, Object>> topProducts = productCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> Map.<String, Object>of("productId", e.getKey(), "totalQuantity", e.getValue()))
                .collect(Collectors.toList());

        return Map.of(
                "totalOrders", orders.size(),
                "totalSpent", totalSpent,
                "mostBoughtProducts", topProducts
        );
    }

    /** Stats for a seller: total revenue, best-selling products */
    public Map<String, Object> getSellerStats(String sellerId) {
        List<Order> orders = orderRepository.findByItemsSellerId(sellerId);

        double totalRevenue = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .flatMap(o -> o.getItems().stream())
                .filter(i -> i.getSellerId().equals(sellerId))
                .mapToDouble(OrderItem::getSubtotal)
                .sum();

        Map<String, Long> productSales = orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .flatMap(o -> o.getItems().stream())
                .filter(i -> i.getSellerId().equals(sellerId))
                .collect(Collectors.groupingBy(OrderItem::getProductId,
                        Collectors.summingLong(OrderItem::getQuantity)));

        List<Map<String, Object>> bestSellers = productSales.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> Map.<String, Object>of("productId", e.getKey(), "totalSold", e.getValue()))
                .collect(Collectors.toList());

        return Map.of(
                "totalOrdersReceived", orders.size(),
                "totalRevenue", totalRevenue,
                "bestSellingProducts", bestSellers
        );
    }

    // ── Common ────────────────────────────────────────────────────────────────

    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }
}
