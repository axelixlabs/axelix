package com.nucleonforge.axile.autoconfiguration.spring;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.spring.master.AxileMetadataEndpoint;

/**
 * Auto-configuration for the {@link AxileMetadataEndpoint}.
 * <p>
 *
 * @since 18.09.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration
public class AxileMetadataEndpointConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AxileMetadataEndpoint axileMetadataEndpoint() {
        return new AxileMetadataEndpoint();
    }
}
