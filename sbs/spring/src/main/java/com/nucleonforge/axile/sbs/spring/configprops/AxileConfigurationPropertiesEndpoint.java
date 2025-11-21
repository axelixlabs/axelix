package com.nucleonforge.axile.sbs.spring.configprops;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

import com.nucleonforge.axile.common.api.AxileConfigPropsFeed;

/**
 * Custom Actuator endpoint exposing the application's {@code @ConfigurationProperties}
 * data from the standard Spring Boot Actuator endpoint.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 */
@Endpoint(id = "axile-configprops")
public class AxileConfigurationPropertiesEndpoint {

    private final ConfigurationPropertiesCache configurationPropertiesCache;

    public AxileConfigurationPropertiesEndpoint(ConfigurationPropertiesCache cache) {
        this.configurationPropertiesCache = cache;
    }

    @ReadOperation
    public AxileConfigPropsFeed configurationProperties() {
        return configurationPropertiesCache.getAxileConfigProps();
    }

    @ReadOperation
    public AxileConfigPropsFeed configurationPropertiesByPrefix(@Selector String prefix) {
        return configurationPropertiesCache.getAxileConfigPropsByPrefix(prefix);
    }
}
