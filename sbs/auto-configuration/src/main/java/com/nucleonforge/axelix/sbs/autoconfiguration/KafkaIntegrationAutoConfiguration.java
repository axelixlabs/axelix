/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.autoconfiguration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;

import com.nucleonforge.axelix.sbs.spring.integrations.IntegrationComponentDiscoverer;
import com.nucleonforge.axelix.sbs.spring.integrations.kafka.KafkaConsumerIntegration;
import com.nucleonforge.axelix.sbs.spring.integrations.kafka.KafkaConsumerIntegrationDiscoverer;

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
