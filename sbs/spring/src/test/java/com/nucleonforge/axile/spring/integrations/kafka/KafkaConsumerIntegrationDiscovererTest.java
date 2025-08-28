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

import com.nucleonforge.axile.spring.integrations.AbstractIntegration;
import com.nucleonforge.axile.spring.integrations.IntegrationComponentDiscoverer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link KafkaConsumerIntegrationDiscoverer}, verifying
 * that Kafka consumer endpoints annotated with {@link org.springframework.kafka.annotation.KafkaListener}
 * are correctly discovered and mapped into {@link KafkaConsumerIntegration} instances.
 *
 * @author Nikita Kirillov
 * @since 24.07.2025
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

        assertThat(integration)
                .isNotNull()
                .returns("Kafka TCP Binary", KafkaConsumerIntegration::protocol)
                .returns("first-group", KafkaConsumerIntegration::getGroupId)
                .returns(true, KafkaConsumerIntegration::isBatchListener)
                .returns(false, KafkaConsumerIntegration::isAutoStartup)
                .returns(3, KafkaConsumerIntegration::getConcurrency)
                .returns(ContainerProperties.AckMode.BATCH, KafkaConsumerIntegration::getAckMode)
                .returns(false, KafkaConsumerIntegration::isRunning)
                .returns(false, KafkaConsumerIntegration::isPaused);
        assertThat(integration.getTopics()).containsExactlyInAnyOrder("topic-1", "topic-2");
        assertThat(integration.getAssignedPartitions()).isNotNull();
    }

    @Test
    void shouldDiscoverSecondKafkaListenerIntegrationWithExpectedProperties() {
        Set<KafkaConsumerIntegration> integrations = discoverer.discoverIntegrations();

        KafkaConsumerIntegration integration = integrations.stream()
                .filter(i -> i.networkAddress().contains("second-listener-id"))
                .findFirst()
                .orElseThrow();

        assertThat(integration)
                .isNotNull()
                .returns("Kafka TCP Binary", KafkaConsumerIntegration::protocol)
                .returns("second-group", KafkaConsumerIntegration::getGroupId)
                .returns(false, KafkaConsumerIntegration::isBatchListener)
                .returns(true, KafkaConsumerIntegration::isAutoStartup)
                .returns(1, KafkaConsumerIntegration::getConcurrency)
                .returns(ContainerProperties.AckMode.BATCH, KafkaConsumerIntegration::getAckMode)
                .returns(true, KafkaConsumerIntegration::isRunning)
                .returns(false, KafkaConsumerIntegration::isPaused);
        assertThat(integration.getTopics()).containsExactlyInAnyOrder("topic-3", "topic-4");
        assertThat(integration.getAssignedPartitions()).isNotNull();
    }

    @Test
    void shouldHaveCorrectGroup() {
        Set<KafkaConsumerIntegration> integrations = discoverer.discoverIntegrations();

        Map<String, List<KafkaConsumerIntegration>> groupedByEntity =
                integrations.stream().collect(Collectors.groupingBy(KafkaConsumerIntegration::getGroupId));

        assertThat(groupedByEntity.get("first-group"))
                .isNotNull()
                .extracting(KafkaConsumerIntegration::networkAddress)
                .contains("kafka://first-listener-id")
                .contains("kafka://third-listener-id");

        assertThat(groupedByEntity.get("second-group"))
                .isNotNull()
                .extracting(KafkaConsumerIntegration::networkAddress)
                .contains("kafka://second-listener-id");
    }

    @Test
    void shouldDiscoverMultipleKafkaListenersFromOneBean() {
        Set<KafkaConsumerIntegration> integrations = discoverer.discoverIntegrations();

        KafkaConsumerIntegration integration1 = integrations.stream()
                .filter(i -> i.networkAddress().contains("first-multi-listener-id"))
                .findFirst()
                .orElseThrow();

        assertThat(integration1)
                .isNotNull()
                .returns("Kafka TCP Binary", KafkaConsumerIntegration::protocol)
                .returns("first-group-multi-listener", KafkaConsumerIntegration::getGroupId);

        KafkaConsumerIntegration integration2 = integrations.stream()
                .filter(i -> i.networkAddress().contains("second-multi-listener-id"))
                .findFirst()
                .orElseThrow();

        assertThat(integration2)
                .isNotNull()
                .returns("Kafka TCP Binary", KafkaConsumerIntegration::protocol)
                .returns("second-group-multi-listener", KafkaConsumerIntegration::getGroupId);
    }

    @Test
    void shouldDiscoverAllKafkaConsumerGroups() {
        Set<KafkaConsumerIntegration> integrations = discoverer.discoverIntegrations();

        assertThat(integrations)
                .extracting(KafkaConsumerIntegration::getGroupId)
                .contains("first-group", "second-group", "first-group-multi-listener", "second-group-multi-listener");
    }

    @Test
    void shouldDiscoverNullGroupIdKafkaListener() {
        Set<KafkaConsumerIntegration> integrations = discoverer.discoverIntegrations();

        KafkaConsumerIntegration integration = integrations.stream()
                .filter(i -> i.networkAddress().contains("null-groupId-listener-id"))
                .findFirst()
                .orElseThrow();

        assertThat(integration)
                .isNotNull()
                .returns("Kafka TCP Binary", KafkaConsumerIntegration::protocol)
                .returns("kafka://null-groupId-listener-id", AbstractIntegration::networkAddress)
                .returns("null-groupId-listener-id", KafkaConsumerIntegration::getGroupId);
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

    public static class NullGroupIdKafkaListener {

        @KafkaListener(id = "null-groupId-listener-id", topics = "null-groupId-listener-topic-1")
        public void listen(String message) {}
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
        public NullGroupIdKafkaListener nullGroupIdKafkaListener() {
            return new NullGroupIdKafkaListener();
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
