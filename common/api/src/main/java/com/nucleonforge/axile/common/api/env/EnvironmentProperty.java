package com.nucleonforge.axile.common.api.env;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response to env/property/{propertyName} actuator endpoint.
 *
 * @param property        The resolved property with its value and source.
 * @param activeProfiles  The currently active Spring Boot application profiles, not specific to this property.
 * @param defaultProfiles The default Spring Boot application profiles, not specific to this property.
 * @param propertySources The property sources that contributed to resolving this property.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/env.html">Env Endpoint</a>
 * @since 02.09.2025
 * @author Nikita Kirillov
 */
public record EnvironmentProperty(
        @JsonProperty("property") Property property,
        @JsonProperty("activeProfiles") List<String> activeProfiles,
        @JsonProperty("defaultProfiles") List<String> defaultProfiles,
        @JsonProperty("propertySources") List<SourceEntry> propertySources) {

    public record Property(String source, String value) {}

    public record SourceEntry(
            @JsonProperty("name") String sourceName, @JsonProperty("property") @Nullable PropertyValue property) {}
}
