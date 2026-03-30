package com.letsplay.userservice.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class UserEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private UserEventProducer userEventProducer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userEventProducer = new UserEventProducer(kafkaTemplate);
    }

    @Test
    void sendUserCreatedEvent_ShouldPublishExpectedPayload() {
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

        userEventProducer.sendUserCreatedEvent("u1", "alice@example.com", "client");

        verify(kafkaTemplate).send(eq("user-events"), eq("u1"), payloadCaptor.capture());
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) payloadCaptor.getValue();
        assertEquals("USER_CREATED", payload.get("eventType"));
        assertEquals("u1", payload.get("userId"));
        assertEquals("alice@example.com", payload.get("email"));
        assertEquals("client", payload.get("role"));
        assertTrue(payload.containsKey("timestamp"));
    }

    @Test
    void sendUserDeletedEvent_ShouldPublishExpectedPayload() {
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

        userEventProducer.sendUserDeletedEvent("u1");

        verify(kafkaTemplate).send(eq("user-events"), eq("u1"), payloadCaptor.capture());
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) payloadCaptor.getValue();
        assertEquals("USER_DELETED", payload.get("eventType"));
        assertEquals("u1", payload.get("userId"));
        assertTrue(payload.containsKey("timestamp"));
    }

    @Test
    void sendUserUpdatedEvent_ShouldPublishExpectedPayload() {
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

        userEventProducer.sendUserUpdatedEvent("u1", "alice@example.com", "seller");

        verify(kafkaTemplate).send(eq("user-events"), eq("u1"), payloadCaptor.capture());
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) payloadCaptor.getValue();
        assertEquals("USER_UPDATED", payload.get("eventType"));
        assertEquals("u1", payload.get("userId"));
        assertEquals("alice@example.com", payload.get("email"));
        assertEquals("seller", payload.get("role"));
        assertTrue(payload.containsKey("timestamp"));
    }
}
