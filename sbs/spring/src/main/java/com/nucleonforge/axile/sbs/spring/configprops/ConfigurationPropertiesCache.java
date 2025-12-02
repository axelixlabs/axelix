package com.nucleonforge.axile.sbs.spring.configprops;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;

import com.nucleonforge.axile.common.api.ConfigPropsFeed;

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

    private final ConfigurationPropertiesConverter configurationPropertiesConverter;

    @Nullable
    private volatile ConfigPropsFeed cachedResult;

    public ConfigurationPropertiesCache(
            ConfigurationPropertiesReportEndpoint delegate,
            ConfigurationPropertiesConverter configurationPropertiesConverter) {
        this.delegate = delegate;
        this.configurationPropertiesConverter = configurationPropertiesConverter;
    }

    public ConfigPropsFeed getAxileConfigProps() {
        if (cachedResult == null) {
            synchronized (this) {
                if (cachedResult == null) {
                    cachedResult = configurationPropertiesConverter.convert(delegate.configurationProperties());
                }
            }
        }
        return cachedResult;
    }
}
