package com.nucleonforge.axile.spring.integrations.kafka;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.common.TopicPartition;

import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListenerContainer;

import com.nucleonforge.axile.spring.integrations.IntegrationComponentDiscoverer;

/**
 * {@link IntegrationComponentDiscoverer} for discovering Kafka consumer integrations
 * based on listeners registered in the {@link KafkaListenerEndpointRegistry}.
 *
 * <p>Collects information about the configuration and state of {@link MessageListenerContainer}
 * instances and transforms them into {@link KafkaConsumerIntegration} objects.</p>
 *
 * @since 24.07.2025
 * @author Nikita Kirillov
 */
public class KafkaConsumerIntegrationDiscoverer implements IntegrationComponentDiscoverer<KafkaConsumerIntegration> {

    private static final Log logger = LogFactory.getLog(KafkaConsumerIntegrationDiscoverer.class);

    private final KafkaListenerEndpointRegistry registry;

    public KafkaConsumerIntegrationDiscoverer(KafkaListenerEndpointRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Set<KafkaConsumerIntegration> discoverIntegrations() {
        Set<KafkaConsumerIntegration> integrations = new HashSet<>();

        for (MessageListenerContainer container : registry.getAllListenerContainers()) {
            ContainerProperties props = container.getContainerProperties();
            String listenerId = container.getListenerId();
            String groupId = props.getGroupId();
            boolean autoStartup = container.isAutoStartup();
            int concurrency = resolveConcurrency(container);
            ContainerProperties.AckMode ackMode = props.getAckMode();
            boolean batchListener = isBatchListener(container);
            boolean running = container.isRunning();
            boolean paused = container.isContainerPaused();
            Set<TopicPartition> assignedPartitions = resolveAssignedPartitions(container);
            Set<String> topics = resolveTopics(props);

            if (groupId == null || groupId.isEmpty()) {
                logger.warn("Kafka listener " + listenerId + " has no group.id configured");
                continue;
            }

            KafkaConsumerIntegration integration = new KafkaConsumerIntegration(
                            listenerId, "Kafka TCP Binary", "Apache Kafka", groupId, ackMode)
                    .setAutoStartup(autoStartup)
                    .setConcurrency(concurrency)
                    .setBatchListener(batchListener)
                    .setRunning(running)
                    .setPaused(paused)
                    .setTopics(topics)
                    .setAssignedPartitions(assignedPartitions);

            integrations.add(integration);
        }

        return integrations;
    }

    private int resolveConcurrency(MessageListenerContainer container) {
        if (container instanceof ConcurrentMessageListenerContainer<?, ?> cmlc) {
            return cmlc.getConcurrency();
        }
        return 1;
    }

    private Set<TopicPartition> resolveAssignedPartitions(MessageListenerContainer container) {
        return container.getAssignedPartitions() != null
                ? new HashSet<>(container.getAssignedPartitions())
                : Collections.emptySet();
    }

    private Set<String> resolveTopics(ContainerProperties props) {
        Set<String> topics = new HashSet<>();

        Optional.ofNullable(props.getTopics()).ifPresent(t -> topics.addAll(Arrays.asList(t)));

        Optional.ofNullable(props.getTopicPartitions())
                .ifPresent(tps -> Arrays.stream(tps).forEach(tp -> topics.add(tp.getTopic())));

        Optional.ofNullable(props.getTopicPattern()).ifPresent(pattern -> topics.add(pattern.pattern()));

        return topics;
    }

    private boolean isBatchListener(final MessageListenerContainer container) {
        Object messageListener = container.getContainerProperties().getMessageListener();
        return messageListener instanceof BatchMessageListener;
    }
}
