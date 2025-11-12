package com.nucleonforge.axile.sbs.spring.env;

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.actuate.env.EnvironmentEndpoint.EnvironmentDescriptor;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Custom Spring Boot Actuator endpoint providing an extended view of the application's environment.
 *
 * @since 21.10.2025
 * @author Nikita Kirillov
 */
@RestControllerEndpoint(id = "axile-env")
public class AxileEnvironmentEndpoint {

    private final EnvironmentEndpoint delegate;

    private final EnvPropertyEnricher envPropertyEnricher;

    public AxileEnvironmentEndpoint(EnvironmentEndpoint delegate, EnvPropertyEnricher envPropertyEnricher) {
        this.delegate = delegate;
        this.envPropertyEnricher = envPropertyEnricher;
    }

    @GetMapping
    public AxileEnvironmentDescriptor environment(@Nullable String pattern) {
        EnvironmentDescriptor originalDescriptor = delegate.environment(pattern);

        return envPropertyEnricher.enrich(originalDescriptor);
    }

    public record AxileEnvironmentDescriptor(
            List<String> activeProfiles,
            List<String> defaultProfiles,
            List<AxilePropertySourceDescriptor> propertySources) {}

    public record AxilePropertySourceDescriptor(String name, Map<String, AxilePropertyValueDescriptor> properties) {}

    public record AxilePropertyValueDescriptor(
            Object value, String origin, boolean isPrimary, @Nullable String configPropsBeanName) {}
}
