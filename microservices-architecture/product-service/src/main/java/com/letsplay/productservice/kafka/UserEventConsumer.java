package com.letsplay.productservice.kafka;

import com.letsplay.productservice.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(UserEventConsumer.class);
    private final ProductService productService;

    public UserEventConsumer(ProductService productService) {
        this.productService = productService;
    }

    @KafkaListener(topics = "user-events", groupId = "product-service-group")
    public void handleUserEvent(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        String userId = (String) event.get("userId");

        logger.info("Received user event: {} for user: {}", eventType, userId);

        if ("USER_DELETED".equals(eventType)) {
            // When a user is deleted, delete all their products
            productService.deleteProductsByUserId(userId);
            logger.info("Deleted all products for deleted user: {}", userId);
        }
    }
}
