package com.nucleonforge.axile.sbs.spring.configprops;

import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesDescriptor;

import com.nucleonforge.axile.common.api.AxileConfigPropsFeed;

/**
 * Interface that is capable to convert values from type {@code ConfigurationPropertiesDescriptor}
 * to type {@code AxileConfigPropsFeed}.
 *
 * @author Sergey Cherkasov
 */
public interface ConfigurationPropertiesConverter {

    /**
     * Converts the original configprops response of type {@code ConfigurationPropertiesDescriptor}
     * to type {@code AxileConfigPropsFeed}
     *
     * @param originalDescriptor the original {@code @ConfigurationProperties} descriptor from Spring Boot
     * @return converted {@code @ConfigurationProperties} descriptor
     */
    AxileConfigPropsFeed convert(ConfigurationPropertiesDescriptor originalDescriptor);
}
