package com.letsplay.mediaservice.kafka;

import com.letsplay.mediaservice.service.MediaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ProductEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(ProductEventConsumer.class);
    private final MediaService mediaService;

    public ProductEventConsumer(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @KafkaListener(topics = "product-events", groupId = "media-service-group")
    public void handleProductEvent(Map<String, Object> event) {
        String eventType = (String) event.get("eventType");
        String productId = (String) event.get("productId");

        logger.info("Received product event: {} for product: {}", eventType, productId);

        if ("PRODUCT_DELETED".equals(eventType)) {
            // When a product is deleted, delete all associated media
            mediaService.deleteMediaByProductId(productId);
            logger.info("Deleted all media for deleted product: {}", productId);
        }
    }
}
