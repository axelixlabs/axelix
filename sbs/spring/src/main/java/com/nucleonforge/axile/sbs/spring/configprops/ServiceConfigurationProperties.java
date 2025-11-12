package com.nucleonforge.axile.sbs.spring.configprops;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesDescriptor;

public class ServiceConfigurationProperties {
    private static final String CACHE_NAME = "axile-configprops";

    private final ConfigurationPropertiesReportEndpoint delegate;
    private final ConcurrentMap<String, ConfigurationPropertiesDescriptor> source = new ConcurrentHashMap<>();

    public ServiceConfigurationProperties(ConfigurationPropertiesReportEndpoint delegate) {
        System.out.println("Loading configuration properties into cache...");
        this.delegate = delegate;
        this.source.put(CACHE_NAME, delegate.configurationProperties());
    }

    public ConfigurationPropertiesDescriptor getConfigurationProperties() {
        return Objects.requireNonNull(source.get(CACHE_NAME));
    }

    public ConfigurationPropertiesDescriptor getConfigurationPropertiesWithPrefix(String prefix) {
        return delegate.configurationPropertiesWithPrefix(prefix);
    }
}
