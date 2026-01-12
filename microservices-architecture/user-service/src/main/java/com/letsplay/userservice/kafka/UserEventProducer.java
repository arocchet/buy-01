package com.letsplay.userservice.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(UserEventProducer.class);
    private static final String TOPIC = "user-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public UserEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUserCreatedEvent(String userId, String email, String role) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "USER_CREATED");
        event.put("userId", userId);
        event.put("email", email);
        event.put("role", role);
        event.put("timestamp", System.currentTimeMillis());

        kafkaTemplate.send(TOPIC, userId, event);
        logger.info("Sent USER_CREATED event for user: {}", userId);
    }

    public void sendUserDeletedEvent(String userId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "USER_DELETED");
        event.put("userId", userId);
        event.put("timestamp", System.currentTimeMillis());

        kafkaTemplate.send(TOPIC, userId, event);
        logger.info("Sent USER_DELETED event for user: {}", userId);
    }

    public void sendUserUpdatedEvent(String userId, String email, String role) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "USER_UPDATED");
        event.put("userId", userId);
        event.put("email", email);
        event.put("role", role);
        event.put("timestamp", System.currentTimeMillis());

        kafkaTemplate.send(TOPIC, userId, event);
        logger.info("Sent USER_UPDATED event for user: {}", userId);
    }
}
