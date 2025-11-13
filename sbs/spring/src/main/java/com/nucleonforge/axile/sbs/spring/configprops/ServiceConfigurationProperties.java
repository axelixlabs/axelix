package com.nucleonforge.axile.sbs.spring.configprops;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesDescriptor;

/**
 * Service caching the application's {@code @ConfigurationProperties}
 * data from the standard Spring Boot Actuator endpoint.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 */
public class ServiceConfigurationProperties {
    private static final String CACHE_NAME = "axile-configprops";

    private final ConfigurationPropertiesReportEndpoint delegate;
    private final ConcurrentMap<String, ConfigurationPropertiesDescriptor> source;

    public ServiceConfigurationProperties(ConfigurationPropertiesReportEndpoint delegate) {
        this.delegate = delegate;
        this.source = new ConcurrentHashMap<>();
    }

    public ConfigurationPropertiesDescriptor getConfigurationProperties() {
        return source.computeIfAbsent(CACHE_NAME, k -> delegate.configurationProperties());
    }
}
