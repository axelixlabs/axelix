package com.nucleonforge.axile.spring.integrations.kafka;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListenerContainer;

import com.nucleonforge.axile.spring.integrations.IntegrationComponentDiscoverer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link KafkaConsumerIntegrationDiscoverer}, verifying
 * that Kafka consumer endpoints annotated with {@link org.springframework.kafka.annotation.KafkaListener}
 * are correctly discovered and mapped into {@link KafkaConsumerIntegration} instances.
 *
 * @since 24.07.2025
 * @author Nikita Kirillov
 */
@SpringBootTest
@EnableKafka
@Import(KafkaConsumerIntegrationDiscovererTest.KafkaConsumerIntegrationDiscovererTestConfiguration.class)
class KafkaConsumerIntegrationDiscovererTest {

    @Autowired
    private IntegrationComponentDiscoverer<KafkaConsumerIntegration> discoverer;

    @Test
    void shouldDiscoverFirstKafkaListenerIntegrationWithExpectedProperties() {
        Set<KafkaConsumerIntegration> integrations = discoverer.discoverIntegrations();

        KafkaConsumerIntegration integration = integrations.stream()
                .filter(i -> i.networkAddress().contains("first-listener-id"))
                .findFirst()
                .orElseThrow();

        assertEquals("Kafka TCP Binary", integration.protocol());
        assertEquals("first-group", integration.getGroupId());
        assertTrue(integration.getTopics().contains("topic-1"));
        assertTrue(integration.getTopics().contains("topic-2"));
        assertTrue(integration.isBatchListener());
        assertFalse(integration.isAutoStartup());
        assertEquals(3, integration.getConcurrency());
        assertEquals(ContainerProperties.AckMode.BATCH, integration.getAckMode());
        assertFalse(integration.isRunning());
        assertFalse(integration.isPaused());
        assertNotNull(integration.getAssignedPartitions());
    }

    @Test
    void shouldDiscoverSecondKafkaListenerIntegrationWithExpectedProperties() {
        Set<KafkaConsumerIntegration> integrations = discoverer.discoverIntegrations();

        KafkaConsumerIntegration integration = integrations.stream()
                .filter(i -> i.networkAddress().contains("second-listener-id"))
                .findFirst()
                .orElseThrow();

        assertEquals("Kafka TCP Binary", integration.protocol());
        assertEquals("second-group", integration.getGroupId());
        assertTrue(integration.getTopics().contains("topic-3"));
        assertTrue(integration.getTopics().contains("topic-4"));
        assertFalse(integration.isBatchListener());
        assertTrue(integration.isAutoStartup());
        assertEquals(1, integration.getConcurrency());
        assertEquals(ContainerProperties.AckMode.BATCH, integration.getAckMode());
        assertTrue(integration.isRunning());
        assertFalse(integration.isPaused());
        assertNotNull(integration.getAssignedPartitions());
    }

    @Test
    void shouldHaveCorrectGroup() {
        Set<KafkaConsumerIntegration> integrations = discoverer.discoverIntegrations();

        Map<String, List<KafkaConsumerIntegration>> groupedByEntity =
                integrations.stream().collect(Collectors.groupingBy(KafkaConsumerIntegration::getGroupId));

        // For first-group
        List<KafkaConsumerIntegration> firstGroup = groupedByEntity.get("first-group");
        assertNotNull(firstGroup);
        List<String> topicsFirstGroup = firstGroup.stream()
                .map(KafkaConsumerIntegration::networkAddress)
                .toList();
        assertTrue(topicsFirstGroup.stream().anyMatch(t -> t.contains("first-listener-id")));

        // For second-group
        List<KafkaConsumerIntegration> secondGroup = groupedByEntity.get("second-group");
        assertNotNull(secondGroup);
        assertTrue(secondGroup.stream().allMatch(i -> i.networkAddress().contains("second-listener-id")));
    }

    @Test
    void shouldDiscoverMultipleKafkaListenersFromOneBean() {
        Set<KafkaConsumerIntegration> integrations = discoverer.discoverIntegrations();

        KafkaConsumerIntegration integration1 = integrations.stream()
                .filter(i -> i.networkAddress().contains("first-multi-listener-id"))
                .findFirst()
                .orElseThrow();

        assertEquals("Kafka TCP Binary", integration1.protocol());
        assertEquals("first-group-multi-listener", integration1.getGroupId());

        KafkaConsumerIntegration integration2 = integrations.stream()
                .filter(i -> i.networkAddress().contains("second-multi-listener-id"))
                .findFirst()
                .orElseThrow();

        assertEquals("Kafka TCP Binary", integration2.protocol());
        assertEquals("second-group-multi-listener", integration2.getGroupId());
    }

    @Test
    void shouldDiscoverAllKafkaConsumerGroups() {
        Set<KafkaConsumerIntegration> integrations = discoverer.discoverIntegrations();

        Set<String> groups =
                integrations.stream().map(KafkaConsumerIntegration::getGroupId).collect(Collectors.toSet());

        assertTrue(groups.contains("first-group"));
        assertTrue(groups.contains("second-group"));
        assertTrue(groups.contains("first-group-multi-listener"));
        assertTrue(groups.contains("second-group-multi-listener"));
    }

    public static class FirstTestKafkaListener {

        @KafkaListener(
                id = "first-listener-id",
                topics = {"topic-1", "topic-2"},
                groupId = "first-group",
                containerFactory = "batchKafkaListenerContainerFactory",
                autoStartup = "false",
                concurrency = "3")
        public void listen(String message) {}
    }

    public static class SecondTestKafkaListener {

        @KafkaListener(
                id = "second-listener-id",
                topics = {"topic-3", "topic-4"},
                groupId = "second-group",
                autoStartup = "true")
        public void listen(String message) {}
    }

    public static class ThirdTestKafkaListener {

        @KafkaListener(id = "third-listener-id", topics = "topic-1", groupId = "first-group")
        public void listen(String message) {}
    }

    public static class MultiKafkaListener {

        @KafkaListener(
                id = "first-multi-listener-id",
                topics = "multi-listener-topic-1",
                groupId = "first-group-multi-listener")
        public void listen1(String message) {}

        @KafkaListener(
                id = "second-multi-listener-id",
                topics = "multi-listener-topic-2",
                groupId = "second-group-multi-listener")
        public void listen2(String message) {}
    }

    /**
     * Test configuration for KafkaConsumerIntegrationDiscoverer tests.
     *
     * <ul>
     *     <li>Provides a {@link ConcurrentKafkaListenerContainerFactory} bean named
     *          "batchKafkaListenerContainerFactory" with batch listener enabled.</li>
     *     <li>Creates a {@link KafkaConsumerIntegrationDiscoverer} bean used to discover
     *          Kafka consumer integrations from the registered Kafka listeners.</li>
     * </ul>
     *
     * <p>This configuration is used only in integration tests to simulate different
     * Kafka listener scenarios and verify discovery logic.</p>
     */
    @TestConfiguration
    @ConditionalOnClass({KafkaListenerEndpointRegistry.class, MessageListenerContainer.class})
    public static class KafkaConsumerIntegrationDiscovererTestConfiguration {

        @Bean
        public FirstTestKafkaListener firstTestKafkaListener() {
            return new FirstTestKafkaListener();
        }

        @Bean
        public SecondTestKafkaListener secondTestKafkaListener() {
            return new SecondTestKafkaListener();
        }

        @Bean
        public ThirdTestKafkaListener thirdTestKafkaListener() {
            return new ThirdTestKafkaListener();
        }

        @Bean
        public MultiKafkaListener multiKafkaListener() {
            return new MultiKafkaListener();
        }

        @Bean
        public ConcurrentKafkaListenerContainerFactory<String, String> batchKafkaListenerContainerFactory(
                ConsumerFactory<String, String> consumerFactory) {

            ConcurrentKafkaListenerContainerFactory<String, String> factory =
                    new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory);
            factory.setBatchListener(true);
            return factory;
        }

        @Bean
        @ConditionalOnBean(KafkaListenerEndpointRegistry.class)
        public IntegrationComponentDiscoverer<KafkaConsumerIntegration> kafkaConsumerIntegrationDiscoverer(
                KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry) {
            return new KafkaConsumerIntegrationDiscoverer(kafkaListenerEndpointRegistry);
        }
    }
}
