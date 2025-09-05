package com.nucleonforge.axile.common.api.env;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response to env actuator endpoint.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/env.html">Env Endpoint</a>
 * @since 26.08.2025
 * @author Nikita Kirillov
 */
public record EnvironmentFeed(
        List<String> activeProfiles, List<String> defaultProfiles, List<PropertySource> propertySources) {

    @JsonCreator
    public EnvironmentFeed(
            @JsonProperty("activeProfiles") List<String> activeProfiles,
            @JsonProperty("defaultProfiles") List<String> defaultProfiles,
            @JsonProperty("propertySources") List<PropertySource> propertySources) {
        this.activeProfiles = activeProfiles;
        this.defaultProfiles = defaultProfiles;
        this.propertySources = propertySources;
    }

    public record PropertySource(String sourceName, Map<String, PropertyValue> properties) {

        @JsonCreator
        public PropertySource(
                @JsonProperty("name") String sourceName,
                @JsonProperty("properties") Map<String, PropertyValue> properties) {
            this.sourceName = sourceName;
            this.properties = properties;
        }
    }
}
