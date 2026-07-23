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

import java.util.Map;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.kafka.clients.consumer.ConsumerGroupMetadata;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.domain.insights.TypeExternalCall;
import com.axelixlabs.axelix.sbs.spring.core.persistence.SimpleExternalCallRecord;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;
import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionExecutionProfile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration tests for {@link ExternalCallKafkaSendInterceptor}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest
@EmbeddedKafka(topics = {"orders", "events", "notifications", "counted", "ignored", "default-topic"})
@TestPropertySource(
        properties = {
            "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
            "spring.kafka.template.default-topic=default-topic"
        })
class ExternalCallKafkaSendInterceptorTest {

    @Autowired
    private TransactionAccessor transactionAccessor;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @AfterEach
    void clearTransactionStack() {
        transactionAccessor.clearAll();
    }

    @Test
    void shouldRecordSendInsideTransaction() {
        // given.
        transactionAccessor.recordNewTransactionStarted();

        // when.
        kafkaTemplate.send("orders", "payload");

        // then.
        TransactionExecutionProfile profile = transactionAccessor.recordTransactionCompletion();
        assertThat(profile.getRecordedExternalCalls()).singleElement().satisfies(call -> {
            assertThat(call.getType()).isEqualTo(TypeExternalCall.KAFKA);
            assertThat(call.getTarget()).isEqualTo("orders");
            assertThat(call.getDurationMs()).isGreaterThanOrEqualTo(0L);
        });
    }

    @Test
    void shouldRecordEachSend() {
        // given.
        transactionAccessor.recordNewTransactionStarted();

        // when.
        kafkaTemplate.send("orders", "one");
        kafkaTemplate.send("orders", "two");

        // then. Every send is tracked, not just the last one.
        TransactionExecutionProfile profile = transactionAccessor.recordTransactionCompletion();
        assertThat(profile.getRecordedExternalCalls()).hasSize(2);
    }

    @Test
    void shouldResolveTargetFromProducerRecord() {
        // given.
        transactionAccessor.recordNewTransactionStarted();

        // when.
        kafkaTemplate.send(new ProducerRecord<>("events", "key", "value"));

        // then.
        TransactionExecutionProfile profile = transactionAccessor.recordTransactionCompletion();
        assertThat(profile.getRecordedExternalCalls())
                .singleElement()
                .extracting(SimpleExternalCallRecord::getTarget)
                .isEqualTo("events");
    }

    @Test
    void shouldResolveTargetFromMessageHeader() {
        // given.
        transactionAccessor.recordNewTransactionStarted();
        Message<String> message = MessageBuilder.withPayload("payload")
                .setHeader(KafkaHeaders.TOPIC, "notifications")
                .build();

        // when.
        kafkaTemplate.send(message);

        // then.
        TransactionExecutionProfile profile = transactionAccessor.recordTransactionCompletion();
        assertThat(profile.getRecordedExternalCalls())
                .singleElement()
                .extracting(SimpleExternalCallRecord::getTarget)
                .isEqualTo("notifications");
    }

    @Test
    void shouldResolveDefaultTopicForMessageWithoutTopicHeader() {
        // given.
        transactionAccessor.recordNewTransactionStarted();
        Message<String> message = MessageBuilder.withPayload("payload").build();

        // when.
        kafkaTemplate.send(message);

        // then.
        TransactionExecutionProfile profile = transactionAccessor.recordTransactionCompletion();
        assertThat(profile.getRecordedExternalCalls())
                .singleElement()
                .extracting(SimpleExternalCallRecord::getTarget)
                .isEqualTo("default-topic");
    }

    @Test
    void shouldResolveDefaultTopicForSendDefault() {
        // given.
        transactionAccessor.recordNewTransactionStarted();

        // when.
        kafkaTemplate.sendDefault("payload");

        // then.
        TransactionExecutionProfile profile = transactionAccessor.recordTransactionCompletion();
        assertThat(profile.getRecordedExternalCalls())
                .singleElement()
                .extracting(SimpleExternalCallRecord::getTarget)
                .isEqualTo("default-topic");
    }

    @Test
    void shouldDropSendMadeOutsideTransaction() {
        // given. A send performed while no transaction is open, then one within a transaction.
        kafkaTemplate.send("ignored", "payload");
        transactionAccessor.recordNewTransactionStarted();

        // when.
        kafkaTemplate.send("counted", "payload");

        // then. Only the send made within the transaction is recorded.
        TransactionExecutionProfile profile = transactionAccessor.recordTransactionCompletion();
        assertThat(profile.getRecordedExternalCalls())
                .singleElement()
                .extracting(SimpleExternalCallRecord::getTarget)
                .isEqualTo("counted");
    }

    @Test
    void shouldNotRecordNonSendMethods() {
        // given.
        transactionAccessor.recordNewTransactionStarted();

        // when.
        kafkaTemplate.flush();

        // then.
        TransactionExecutionProfile profile = transactionAccessor.recordTransactionCompletion();
        assertThat(profile.getRecordedExternalCalls()).isEmpty();
    }

    @Test
    void shouldNotRecordSendOffsetsToTransaction() throws Throwable {
        // given. sendOffsetsToTransaction also starts with "send" but is not a message send.
        ExternalCallKafkaSendInterceptor subject =
                new ExternalCallKafkaSendInterceptor(transactionAccessor, kafkaTemplate);
        MethodInvocation invocation = mock(MethodInvocation.class);
        when(invocation.getMethod())
                .thenReturn(KafkaOperations.class.getMethod(
                        "sendOffsetsToTransaction", Map.class, ConsumerGroupMetadata.class));
        transactionAccessor.recordNewTransactionStarted();

        // when.
        subject.invoke(invocation);

        // then. The call is passed through without being recorded.
        verify(invocation).proceed();
        TransactionExecutionProfile profile = transactionAccessor.recordTransactionCompletion();
        assertThat(profile.getRecordedExternalCalls()).isEmpty();
    }

    @TestConfiguration
    static class KafkaMonitoringTestConfiguration {

        @Bean
        public TransactionAccessor transactionAccessor() {
            return new TransactionAccessor();
        }

        @Bean
        public KafkaTemplateMonitoringBeanPostProcessor kafkaTemplateMonitoringBeanPostProcessor(
                TransactionAccessor transactionAccessor) {
            return new KafkaTemplateMonitoringBeanPostProcessor(transactionAccessor);
        }
    }
}
