package com.nucleonforge.axile.sbs.autoconfiguration.spring;

import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.sbs.spring.metrics.AxileMetricsEndpoint;

/**
 * Auto-configuration for the {@link AxileMetricsEndpoint}.
 *
 * @since 17.11.2025
 * @author Nikita Kirillov
 */
@AutoConfiguration(after = MetricsAutoConfiguration.class)
@ConditionalOnAvailableEndpoint(endpoint = MetricsEndpoint.class)
public class AxileMetricsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AxileMetricsEndpoint axileMetricsEndpoint(MetricsEndpoint metricsEndpoint, MeterRegistry registry) {
        return new AxileMetricsEndpoint(metricsEndpoint, registry);
    }
}
