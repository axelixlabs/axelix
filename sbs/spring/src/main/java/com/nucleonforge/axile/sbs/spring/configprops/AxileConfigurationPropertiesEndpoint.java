package com.nucleonforge.axile.sbs.spring.configprops;

import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesDescriptor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

/**
 * Custom Actuator endpoint exposing the application's {@code @ConfigurationProperties}
 * data from the standard Spring Boot Actuator endpoint.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 */
@Endpoint(id = "axile-configprops")
public class AxileConfigurationPropertiesEndpoint {

    private final ServiceConfigurationProperties service;

    public AxileConfigurationPropertiesEndpoint(ServiceConfigurationProperties service) {
        this.service = service;
    }

    @ReadOperation
    public ConfigurationPropertiesDescriptor configurationProperties() {
        return service.getConfigurationProperties();
    }
}
