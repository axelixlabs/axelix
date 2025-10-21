package com.nucleonforge.axile.sbs.spring.env;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.actuate.endpoint.OperationResponseBody;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
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

    public AxileEnvironmentEndpoint(EnvironmentEndpoint delegate) {
        this.delegate = delegate;
    }

    @GetMapping
    public EnvironmentDescriptor environment(@Nullable String pattern) {
        EnvironmentEndpoint.EnvironmentDescriptor descriptor = delegate.environment(pattern);

        Map<String, String> primaryMap = new LinkedHashMap<>();
        for (EnvironmentEndpoint.PropertySourceDescriptor source : descriptor.getPropertySources()) {
            for (String key : source.getProperties().keySet()) {
                primaryMap.putIfAbsent(key, source.getName());
            }
        }

        List<PropertySourceDescriptor> sources = descriptor.getPropertySources().stream()
                .map(src -> new PropertySourceDescriptor(
                        src.getName(),
                        src.getProperties().entrySet().stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        entry -> new PropertyValueDescriptor(
                                                entry.getValue().getValue(),
                                                entry.getValue().getOrigin(),
                                                Objects.equals(primaryMap.get(entry.getKey()), src.getName()))))))
                .toList();

        return new EnvironmentDescriptor(descriptor.getActiveProfiles(), sources);
    }

    public record EnvironmentDescriptor(List<String> activeProfiles, List<PropertySourceDescriptor> propertySources)
            implements OperationResponseBody {}

    public record PropertySourceDescriptor(String name, Map<String, PropertyValueDescriptor> properties) {}

    public record PropertyValueDescriptor(Object value, String origin, boolean isPrimary) {}
}
