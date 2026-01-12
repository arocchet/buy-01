package com.letsplay.productservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProductEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(ProductEventProducer.class);
    private static final String TOPIC = "product-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ProductEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendProductCreatedEvent(String productId, String userId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "PRODUCT_CREATED");
        event.put("productId", productId);
        event.put("userId", userId);
        event.put("timestamp", System.currentTimeMillis());

        kafkaTemplate.send(TOPIC, productId, event);
        logger.info("Sent PRODUCT_CREATED event for product: {}", productId);
    }

    public void sendProductUpdatedEvent(String productId, String userId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "PRODUCT_UPDATED");
        event.put("productId", productId);
        event.put("userId", userId);
        event.put("timestamp", System.currentTimeMillis());

        kafkaTemplate.send(TOPIC, productId, event);
        logger.info("Sent PRODUCT_UPDATED event for product: {}", productId);
    }

    public void sendProductDeletedEvent(String productId, String userId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "PRODUCT_DELETED");
        event.put("productId", productId);
        event.put("userId", userId);
        event.put("timestamp", System.currentTimeMillis());

        kafkaTemplate.send(TOPIC, productId, event);
        logger.info("Sent PRODUCT_DELETED event for product: {}", productId);
    }
}
