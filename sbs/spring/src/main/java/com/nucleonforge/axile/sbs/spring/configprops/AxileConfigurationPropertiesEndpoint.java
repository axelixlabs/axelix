package com.nucleonforge.axile.sbs.spring.configprops;

import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;

@Endpoint(id = "axile-configprops")
public class AxileConfigurationPropertiesEndpoint {

    private final ServiceConfigurationProperties service;

    public AxileConfigurationPropertiesEndpoint(ServiceConfigurationProperties service) {
        this.service = service;
    }

    @ReadOperation
    public ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesDescriptor configurationProperties() {
        return service.getConfigurationProperties();
    }

    @ReadOperation
    public ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesDescriptor configurationPropertiesWithPrefix(
            @Selector String prefix) {
        return service.getConfigurationPropertiesWithPrefix(prefix);
    }
}
