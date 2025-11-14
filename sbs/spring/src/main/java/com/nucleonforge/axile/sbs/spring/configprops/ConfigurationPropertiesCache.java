package com.nucleonforge.axile.sbs.spring.configprops;

import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesDescriptor;

/**
 * Service caching the application's {@code @ConfigurationProperties}
 * data from the standard Spring Boot Actuator endpoint.
 *
 * @since 13.11.2025
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public class ConfigurationPropertiesCache {

    private final ConfigurationPropertiesReportEndpoint delegate;

    @SuppressWarnings("NullAway")
    private volatile ConfigurationPropertiesDescriptor cachedResult;

    public ConfigurationPropertiesCache(ConfigurationPropertiesReportEndpoint delegate) {
        this.delegate = delegate;
    }

    public ConfigurationPropertiesDescriptor getConfigurationProperties() {
        if (cachedResult == null) {
            synchronized (this) {
                if (cachedResult == null) {
                    cachedResult = delegate.configurationProperties();
                }
            }
        }

        return cachedResult;
    }
}
