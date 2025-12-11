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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response to axile-env actuator endpoint.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/env.html">Env Endpoint</a>
 *
 * @param activeProfiles   the list of currently active Spring profiles.
 * @param defaultProfiles  the list of default Spring profiles.
 * @param propertySources  the list of property sources with their short profiles.
 *
 * @since 26.08.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
public record EnvironmentFeed(
        @JsonProperty("activeProfiles") List<String> activeProfiles,
        @JsonProperty("defaultProfiles") List<String> defaultProfiles,
        @JsonProperty("propertySources") List<PropertySource> propertySources) {

    /**
     * DTO that encapsulates the property source of the given artifact.
     *
     * @param sourceName         the sourceName of the property source.
     * @param sourceDescription  the custom description of this property source, if any.
     * @param properties         the list of property entries.
     */
    public record PropertySource(
            @JsonProperty("sourceName") String sourceName,
            @JsonProperty("sourceDescription") @Nullable String sourceDescription,
            @JsonProperty("properties") List<Property> properties) {}

    /**
     * DTO representing a property value returned by the custom Axile environment endpoint.
     *
     * @param propertyName         the property name.
     * @param value                the string representation of the property's value.
     * @param isPrimary            whether this property value is primary (i.e. this value takes precedence over the other values
     *                             from other property sources).
     * @param configPropsBeanName  the propertyName of the configProps (if any) bean onto which this property maps,
     *                             {@code null} otherwise.
     * @param description          the description from spring-configuration-metadata.json
     * @param deprecation          deprecation related information. If {@code null}, the
     *                             property is not considered deprecated. If not {@code null},
     *                             then the property is considered deprecated.
     */
    public record Property(
            @JsonProperty("propertyName") String propertyName,
            @JsonProperty("value") @Nullable String value,
            @JsonProperty("isPrimary") boolean isPrimary,
            @JsonProperty("configPropsBeanName") @Nullable String configPropsBeanName,
            @JsonProperty("description") @Nullable String description,
            @JsonProperty("deprecation") @Nullable Deprecation deprecation) {}

    /**
     * DTO that encapsulates the deprecation property of the given artifact.
     *
     * @param reason        the reason why the given property is deprecated.
     * @param replacement   the name of the property that potentially aims to replace the given deprecated property.
     */
    public record Deprecation(
            @JsonProperty("reason") @Nullable String reason,
            @JsonProperty("replacement") @Nullable String replacement) {}
}
