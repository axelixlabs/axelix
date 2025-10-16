package com.nucleonforge.axile.sbs.spring.integrations.kafka;

import java.util.HashSet;
import java.util.Set;

import org.apache.kafka.common.TopicPartition;

import org.springframework.kafka.listener.ContainerProperties;

import com.nucleonforge.axile.sbs.spring.integrations.AbstractIntegration;

/**
 * Represents an integration with a Kafka consumer.
 *
 * @author Nikita Kirillov
 * @since 24.07.2025
 */
public class KafkaConsumerIntegration extends AbstractIntegration {

    /**
     * Identifier of the Kafka consumer group this consumer belongs to.
     */
    private String groupId;

    /**
     * Whether the consumer should start automatically on application startup.
     */
    private boolean autoStartup;

    /**
     * The number of concurrent threads for this consumer.
     */
    private int concurrency;

    /**
     * The acknowledgement mode used by the Kafka container.
     */
    private ContainerProperties.AckMode ackMode;

    /**
     * Whether this consumer listens in batch mode.
     */
    private boolean batchListener;

    /**
     * Current running status of the consumer.
     */
    private boolean running;

    /**
     * Whether the consumer is currently paused.
     */
    private boolean paused;

    /**
     * Set of topic names that this consumer subscribes to.
     */
    private Set<String> topics = new HashSet<>();

    /**
     * Set of assigned Kafka topic partitions for this consumer.
     */
    private Set<TopicPartition> assignedPartitions = new HashSet<>();

    public KafkaConsumerIntegration(
            String listenerId,
            String protocol,
            String entityType,
            String groupId,
            ContainerProperties.AckMode ackMode) {
        super("kafka://" + listenerId, protocol, entityType);
        this.groupId = groupId;
        this.ackMode = ackMode;
    }

    public KafkaConsumerIntegration setAutoStartup(boolean autoStartup) {
        this.autoStartup = autoStartup;
        return this;
    }

    public KafkaConsumerIntegration setConcurrency(int concurrency) {
        this.concurrency = concurrency;
        return this;
    }

    public KafkaConsumerIntegration setBatchListener(boolean batchListener) {
        this.batchListener = batchListener;
        return this;
    }

    public KafkaConsumerIntegration setRunning(boolean running) {
        this.running = running;
        return this;
    }

    public KafkaConsumerIntegration setPaused(boolean paused) {
        this.paused = paused;
        return this;
    }

    public KafkaConsumerIntegration setTopics(Set<String> topics) {
        this.topics = topics;
        return this;
    }

    public KafkaConsumerIntegration setAssignedPartitions(Set<TopicPartition> assignedPartitions) {
        this.assignedPartitions = assignedPartitions;
        return this;
    }

    public String getGroupId() {
        return groupId;
    }

    public boolean isAutoStartup() {
        return autoStartup;
    }

    public int getConcurrency() {
        return concurrency;
    }

    public ContainerProperties.AckMode getAckMode() {
        return ackMode;
    }

    public boolean isBatchListener() {
        return batchListener;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isPaused() {
        return paused;
    }

    public Set<String> getTopics() {
        return topics;
    }

    public Set<TopicPartition> getAssignedPartitions() {
        return assignedPartitions;
    }
}
