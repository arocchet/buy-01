package com.letsplay.orderservice.service;

import com.letsplay.orderservice.dto.CartItemRequest;
import com.letsplay.orderservice.exception.BadRequestException;
import com.letsplay.orderservice.exception.ResourceNotFoundException;
import com.letsplay.orderservice.model.Cart;
import com.letsplay.orderservice.model.CartItem;
import com.letsplay.orderservice.repository.CartRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private static final Logger log = LoggerFactory.getLogger(CartService.class);
    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    /**
     * Returns the cart for the user, merging and removing any duplicate documents
     * that may exist (e.g. from a race condition before the unique index was added).
     */
    public Cart getOrCreateCart(String userId) {
        List<Cart> all = cartRepository.findAllByUserId(userId);

        if (all.isEmpty()) {
            return cartRepository.save(new Cart(userId));
        }

        if (all.size() == 1) {
            return all.get(0);
        }

        // Merge all duplicate carts into the first one, then delete the rest
        log.warn("Found {} duplicate carts for userId={}, merging...", all.size(), userId);
        Cart primary = all.get(0);
        for (int i = 1; i < all.size(); i++) {
            Cart dup = all.get(i);
            for (CartItem item : dup.getItems()) {
                Optional<CartItem> existing = primary.getItems().stream()
                        .filter(it -> it.getProductId().equals(item.getProductId()))
                        .findFirst();
                if (existing.isPresent()) {
                    existing.get().setQuantity(existing.get().getQuantity() + item.getQuantity());
                } else {
                    primary.getItems().add(item);
                }
            }
            cartRepository.deleteById(dup.getId());
        }
        return cartRepository.save(primary);
    }

    public Cart addItem(String userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = new CartItem(
                    request.getProductId(),
                    request.getProductName(),
                    request.getQuantity(),
                    request.getUnitPrice(),
                    request.getSellerId()
            );
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    public Cart updateItemQuantity(String userId, String productId, int quantity) {
        if (quantity < 1) {
            throw new BadRequestException("Quantity must be at least 1");
        }
        Cart cart = getOrCreateCart(userId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart: " + productId));
        item.setQuantity(quantity);
        return cartRepository.save(cart);
    }

    public Cart removeItem(String userId, String productId) {
        Cart cart = getOrCreateCart(userId);
        boolean removed = cart.getItems().removeIf(item -> item.getProductId().equals(productId));
        if (!removed) {
            throw new ResourceNotFoundException("Item not found in cart: " + productId);
        }
        return cartRepository.save(cart);
    }

    public Cart clearCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        return cartRepository.save(cart);
    }
}
