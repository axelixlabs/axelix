package com.nucleonforge.axile.sbs.spring.configprops;

import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesDescriptor;

import com.nucleonforge.axile.sbs.spring.configprops.ConfigurationPropertiesCache.AxileConfigurationPropertiesDescriptor;

/**
 * Interface that is capable to convert values from type {@code ConfigurationPropertiesDescriptor}
 * to type {@code AxileConfigurationPropertiesDescriptor}.
 *
 * @author Sergey Cherkasov
 */
public interface ConfigurationPropertiesConverter {

    /**
     * Converts the original configprops response of type {@code ConfigurationPropertiesDescriptor}
     * to type {@code AxileConfigurationPropertiesDescriptor}
     *
     * @param originalDescriptor the original {@code @ConfigurationProperties} descriptor from Spring Boot
     * @return converted {@code @ConfigurationProperties} descriptor
     */
    AxileConfigurationPropertiesDescriptor convert(ConfigurationPropertiesDescriptor originalDescriptor);
}
