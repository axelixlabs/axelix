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
package com.axelixlabs.axelix.sbs.spring.core.persistence.kafka;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jspecify.annotations.Nullable;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

import com.axelixlabs.axelix.common.domain.insights.TypeExternalCall;
import com.axelixlabs.axelix.sbs.spring.core.persistence.SimpleExternalCallRecord;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;

/**
 * {@link MethodInterceptor} that records every {@code send} performed by a {@link KafkaTemplate} while a
 * transaction is open, so that the Kafka calls made inside the transaction become visible and feed its
 * min/max/avg statistics. It measures the {@code send()} call itself — the time it blocks the transaction
 * thread.
 * <p>
 * Whether the call actually belongs to a transaction is decided by the {@link TransactionAccessor}: calls
 * made outside of any transaction are silently dropped by it.
 *
 * @author Sergey Cherkasov
 */
class ExternalCallKafkaSendInterceptor implements MethodInterceptor {

    private final TransactionAccessor transactionAccessor;
    private final KafkaTemplate<?, ?> kafkaTemplate;

    ExternalCallKafkaSendInterceptor(TransactionAccessor transactionAccessor, KafkaTemplate<?, ?> kafkaTemplate) {
        this.transactionAccessor = transactionAccessor;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    @Nullable
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();

        if (!isSendMethod(method)) {
            return invocation.proceed();
        }

        String target = resolveTarget(method, invocation.getArguments());
        long startNanos = System.nanoTime();

        try {
            return invocation.proceed();
        } finally {
            long duration = System.nanoTime() - startNanos;
            transactionAccessor.recordExternalCall(
                    new SimpleExternalCallRecord(TypeExternalCall.KAFKA, target, duration / 1_000_000));
        }
    }

    // Matches only message sends. Excludes sendOffsetsToTransaction, which also starts with "send" but
    // commits consumer offsets into a Kafka transaction rather than publishing a message (it has no topic).
    private static boolean isSendMethod(Method method) {
        String name = method.getName();
        return name.equals("send") || name.equals("sendDefault");
    }

    private String resolveTarget(Method method, Object[] arguments) {
        if (method.getName().equals("sendDefault")) {
            return defaultTopicOrEmpty();
        }

        if (arguments.length > 0) {
            Object first = arguments[0];

            if (first instanceof String topicName) {
                return topicName;
            }
            if (first instanceof ProducerRecord<?, ?> producerRecord) {
                return producerRecord.topic();
            }
            if (first instanceof Message<?> message) {
                Object topic = message.getHeaders().get(KafkaHeaders.TOPIC);
                return topic != null ? topic.toString() : defaultTopicOrEmpty();
            }
        }

        return "";
    }

    private String defaultTopicOrEmpty() {
        String defaultTopic = kafkaTemplate.getDefaultTopic();
        return defaultTopic != null ? defaultTopic : "";
    }
}
