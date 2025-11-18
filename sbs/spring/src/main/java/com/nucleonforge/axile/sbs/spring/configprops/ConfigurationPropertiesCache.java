package com.nucleonforge.axile.sbs.spring.configprops;

import java.util.List;
import java.util.Map;

import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;

import com.nucleonforge.axile.common.api.KeyValue;

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

    @SuppressWarnings("NullAway")
    private volatile AxileConfigurationPropertiesDescriptor cachedResult;

    public ConfigurationPropertiesCache(
            ConfigurationPropertiesReportEndpoint delegate,
            ConfigurationPropertiesConverter configurationPropertiesConverter) {
        this.delegate = delegate;
        this.configurationPropertiesConverter = configurationPropertiesConverter;
    }

    public AxileConfigurationPropertiesDescriptor getAxileConfigProps() {
        if (cachedResult == null) {
            synchronized (this) {
                if (cachedResult == null) {
                    cachedResult = configurationPropertiesConverter.convert(delegate.configurationProperties());
                }
            }
        }
        return cachedResult;
    }

    public AxileConfigurationPropertiesDescriptor getAxileConfigPropsByPrefix(String prefix) {
        return configurationPropertiesConverter.convert(delegate.configurationPropertiesWithPrefix(prefix));
    }

    public record AxileConfigurationPropertiesDescriptor(
            Map<String, AxileContextConfigurationPropertiesDescriptor> contexts) {}

    public record AxileContextConfigurationPropertiesDescriptor(
            String parentId, Map<String, AxileConfigurationPropertiesBeanDescriptor> beans) {}

    public record AxileConfigurationPropertiesBeanDescriptor(
            String prefix, List<KeyValue> properties, List<KeyValue> inputs) {}
}
