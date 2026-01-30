/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.integrations.kafka;

import java.util.HashSet;
import java.util.Set;

import org.apache.kafka.common.TopicPartition;

import org.springframework.kafka.listener.ContainerProperties;

import com.axelixlabs.axelix.sbs.spring.integrations.AbstractIntegration;

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
