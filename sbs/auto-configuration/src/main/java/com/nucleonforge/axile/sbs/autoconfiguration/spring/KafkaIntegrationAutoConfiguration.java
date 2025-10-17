package com.nucleonforge.axile.sbs.autoconfiguration.spring;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;

import com.nucleonforge.axile.sbs.spring.integrations.IntegrationComponentDiscoverer;
import com.nucleonforge.axile.sbs.spring.integrations.kafka.KafkaConsumerIntegration;
import com.nucleonforge.axile.sbs.spring.integrations.kafka.KafkaConsumerIntegrationDiscoverer;

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
