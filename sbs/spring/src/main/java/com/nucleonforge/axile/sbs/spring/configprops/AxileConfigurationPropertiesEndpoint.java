package com.nucleonforge.axile.sbs.spring.configprops;

import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;

@Endpoint(id = "axile-configprops")
@CacheConfig(cacheNames = "axile-configprops-cache", cacheManager = "axileCacheManager")
public class AxileConfigurationPropertiesEndpoint {

    private final ConfigurationPropertiesReportEndpoint delegate;

    public AxileConfigurationPropertiesEndpoint(ConfigurationPropertiesReportEndpoint delegate) {
        this.delegate = delegate;
    }

    @ReadOperation
    @Cacheable(key = "'configurationProperties'")
    public ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesDescriptor configurationProperties() {
        return delegate.configurationProperties();
    }

    @ReadOperation
    public ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesDescriptor configurationPropertiesWithPrefix(
            @Selector String prefix) {
        return delegate.configurationPropertiesWithPrefix(prefix);
    }
}
