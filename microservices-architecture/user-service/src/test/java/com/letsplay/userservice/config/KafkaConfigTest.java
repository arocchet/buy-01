package com.letsplay.userservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class KafkaConfigTest {

    private KafkaConfig kafkaConfig;

    @BeforeEach
    void setUp() {
        kafkaConfig = new KafkaConfig();
        ReflectionTestUtils.setField(kafkaConfig, "bootstrapServers", "localhost:9092");
    }

    @Test
    void producerFactory_ShouldContainExpectedConfig() {
        ProducerFactory<String, Object> producerFactory = kafkaConfig.producerFactory();
        assertNotNull(producerFactory);

        DefaultKafkaProducerFactory<String, Object> factory = (DefaultKafkaProducerFactory<String, Object>) producerFactory;
        Map<String, Object> config = factory.getConfigurationProperties();
        assertEquals("localhost:9092", config.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
    }

    @Test
    void kafkaTemplate_ShouldBeCreated() {
        KafkaTemplate<String, Object> template = kafkaConfig.kafkaTemplate();
        assertNotNull(template);
    }

    @Test
    void userEventsTopic_ShouldHaveExpectedProperties() {
        NewTopic topic = kafkaConfig.userEventsTopic();
        assertEquals("user-events", topic.name());
        assertEquals(1, topic.numPartitions());
        assertEquals((short) 1, topic.replicationFactor());
    }
}
