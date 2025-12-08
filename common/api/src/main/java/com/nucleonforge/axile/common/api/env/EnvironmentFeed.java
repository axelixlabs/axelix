/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.common.api.env;

import java.util.List;

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

    public record PropertySource(String sourceName, List<Property> properties) {

        @JsonCreator
        public PropertySource(
                @JsonProperty("sourceName") String sourceName, @JsonProperty("properties") List<Property> properties) {
            this.sourceName = sourceName;
            this.properties = properties;
        }
    }

    public record Property(
            String propertyName,
            @Nullable String value,
            boolean isPrimary,
            @Nullable String configPropsBeanName,
            @Nullable String description,
            @Nullable Deprecation deprecation) {}

    public record Deprecation(
            @JsonProperty("reason") @Nullable String reason,
            @JsonProperty("replacement") @Nullable String replacement) {}
}
