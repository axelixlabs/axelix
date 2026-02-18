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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;

import com.axelixlabs.axelix.sbs.spring.core.integration.IntegrationComponentDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.integrations.kafka.KafkaConsumerIntegration;
import com.axelixlabs.axelix.sbs.spring.core.integrations.kafka.KafkaConsumerIntegrationDiscoverer;

/**
 * Auto-configuration for discovering Kafka integrations.
 * <p>
 * This configuration is automatically applied after the standard {@link KafkaAutoConfiguration}.
 * </p>
 *
 * @see KafkaConsumerIntegrationDiscoverer
 * @see KafkaListenerEndpointRegistry
 * @since 25.07.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration(after = KafkaAutoConfiguration.class)
@ConditionalOnClass({KafkaListenerEndpointRegistry.class, MessageListenerContainer.class})
public class KafkaIntegrationAutoConfiguration {

    @Bean
    @ConditionalOnBean(KafkaListenerEndpointRegistry.class)
    @ConditionalOnMissingBean
    public IntegrationComponentDiscoverer<KafkaConsumerIntegration> kafkaConsumerIntegrationDiscoverer(
            KafkaListenerEndpointRegistry registry) {
        return new KafkaConsumerIntegrationDiscoverer(registry);
    }
}
