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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;

import com.axelixlabs.axelix.sbs.spring.core.integration.IntegrationComponentDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.integrations.kafka.KafkaConsumerIntegration;
import com.axelixlabs.axelix.sbs.spring.core.integrations.kafka.KafkaConsumerIntegrationDiscoverer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link KafkaIntegrationAutoConfiguration}
 *
 * @since 10.02.2026
 * @author Nikita Kirillov
 */
class KafkaIntegrationAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(
                    AutoConfigurations.of(KafkaIntegrationAutoConfiguration.class, KafkaAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(IntegrationComponentDiscoverer.class);
            assertThat(context).hasSingleBean(KafkaConsumerIntegrationDiscoverer.class);
            assertThat(context)
                    .getBean(IntegrationComponentDiscoverer.class)
                    .isExactlyInstanceOf(KafkaConsumerIntegrationDiscoverer.class);
        });
    }

    @ParameterizedTest
    @ValueSource(classes = {KafkaListenerEndpointRegistry.class, MessageListenerContainer.class})
    void shouldNotActivateAutoConfiguration_whenRequiredClassMissing(Class<?> toBeExcluded) {
        contextRunner.withClassLoader(new FilteredClassLoader(toBeExcluded)).run(context -> {
            assertThat(context).doesNotHaveBean(KafkaIntegrationAutoConfiguration.class);
            assertThat(context).doesNotHaveBean(IntegrationComponentDiscoverer.class);
        });
    }

    @Test
    void shouldNotCreateKafkaConsumerIntegrationDiscoverer_whenCustomBeanProvided() {
        contextRunner.withUserConfiguration(CustomDiscovererConfig.class).run(context -> {
            assertThat(context).hasSingleBean(IntegrationComponentDiscoverer.class);
            assertThat(context)
                    .getBean(IntegrationComponentDiscoverer.class)
                    .isExactlyInstanceOf(CustomKafkaConsumerIntegrationDiscoverer.class);
        });
    }

    @TestConfiguration
    static class CustomDiscovererConfig {
        @Bean
        public IntegrationComponentDiscoverer<KafkaConsumerIntegration> kafkaConsumerIntegrationDiscoverer(
                KafkaListenerEndpointRegistry registry) {
            return new CustomKafkaConsumerIntegrationDiscoverer(registry);
        }
    }

    static class CustomKafkaConsumerIntegrationDiscoverer extends KafkaConsumerIntegrationDiscoverer {
        public CustomKafkaConsumerIntegrationDiscoverer(KafkaListenerEndpointRegistry registry) {
            super(registry);
        }
    }
}
