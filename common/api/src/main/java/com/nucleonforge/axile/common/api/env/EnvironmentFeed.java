package com.nucleonforge.axile.common.api.env;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response to axile-env actuator endpoint.
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

    public record PropertyValue(
            @Nullable String value,
            @Nullable String origin,
            boolean isPrimary,
            @Nullable String configPropsBeanName,
            @Nullable String validationMessage) {

        @JsonCreator
        public PropertyValue(
                @JsonProperty("value") @Nullable String value,
                @JsonProperty("origin") @Nullable String origin,
                @JsonProperty("isPrimary") boolean isPrimary,
                @JsonProperty("configPropsBeanName") @Nullable String configPropsBeanName,
                @JsonProperty("validationMessage") @Nullable String validationMessage) {
            this.value = value;
            this.origin = origin;
            this.isPrimary = isPrimary;
            this.configPropsBeanName = configPropsBeanName;
            this.validationMessage = validationMessage;
        }
    }
}
