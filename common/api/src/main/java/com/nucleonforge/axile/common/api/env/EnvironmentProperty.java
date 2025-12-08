/*
 * Copyright 2025-present the original author or authors.
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
