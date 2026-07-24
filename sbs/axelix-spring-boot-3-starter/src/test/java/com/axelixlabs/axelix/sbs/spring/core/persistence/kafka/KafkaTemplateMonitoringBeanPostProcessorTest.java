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

import java.util.Arrays;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.axelixlabs.axelix.sbs.spring.core.persistence.transaction.TransactionAccessor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link KafkaTemplateMonitoringBeanPostProcessor}.
 *
 * @author Sergey Cherkasov
 */
class KafkaTemplateMonitoringBeanPostProcessorTest {

    private final KafkaTemplateMonitoringBeanPostProcessor subject =
            new KafkaTemplateMonitoringBeanPostProcessor(new TransactionAccessor());

    @Test
    void shouldInstrumentKafkaTemplate() {
        // given.
        KafkaTemplate<String, String> kafkaTemplate = newKafkaTemplate();

        // when.
        Object result = subject.postProcessAfterInitialization(kafkaTemplate, "kafkaTemplate");

        // then.
        assertThat(AopUtils.isAopProxy(result)).isTrue();
        assertThat(result).isInstanceOf(KafkaTemplate.class);
        assertThat(((Advised) result).getAdvisors())
                .anyMatch(advisor -> advisor.getAdvice() instanceof ExternalCallKafkaSendInterceptor);
    }

    @Test
    void shouldNotInstrumentTheSameKafkaTemplateTwice() {
        // given.
        KafkaTemplate<String, String> kafkaTemplate = newKafkaTemplate();
        Object instrumented = subject.postProcessAfterInitialization(kafkaTemplate, "kafkaTemplate");

        // when.
        Object result = subject.postProcessAfterInitialization(instrumented, "kafkaTemplate");

        // then. A second proxy would make every send be recorded twice.
        assertThat(result).isSameAs(instrumented);
        long interceptorCount = Arrays.stream(((Advised) result).getAdvisors())
                .filter(advisor -> advisor.getAdvice() instanceof ExternalCallKafkaSendInterceptor)
                .count();
        assertThat(interceptorCount).isEqualTo(1);
    }

    private static KafkaTemplate<String, String> newKafkaTemplate() {
        ProducerFactory<String, String> producerFactory =
                () -> new MockProducer<>(true, new StringSerializer(), new StringSerializer());
        return new KafkaTemplate<>(producerFactory);
    }
}
