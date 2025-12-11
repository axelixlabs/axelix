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
package com.nucleonforge.axile.master.api.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.common.api.env.EnvironmentFeed;

/**
 * The feed of the environment used in the application.
 *
 * @param activeProfiles   the list of currently active Spring profiles
 * @param defaultProfiles  the list of default Spring profiles
 * @param propertySources  the list of property sources with their short profiles
 *
 * @see EnvironmentFeed
 * @since 27.08.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
public record EnvironmentFeedResponse(
        List<String> activeProfiles, List<String> defaultProfiles, List<PropertySourceShortProfile> propertySources) {

    /**
     * Short profile of a given property source.
     *
     * @param name         the sourceName of the property source
     * @param description  the custom description of this property source, if any.
     * @param properties   the list of property entries
     */
    public record PropertySourceShortProfile(
            String name, @Nullable String description, List<PropertyEntry> properties) {}

    /**
     * Represents a property value returned by the custom Axile environment endpoint.
     *
     * @param name                 the property name
     * @param value                the string representation of the property's value
     * @param isPrimary            whether this property value is primary (i.e. this value takes precedence over the other values
     *                             from other property sources)
     * @param configPropsBeanName  the propertyName of the configprops (if any) bean onto which this property maps,
     *                             {@code null} otherwise
     * @param description          the description from spring-configuration-metadata.json
     * @param deprecation          deprecation related information. If {@code null}, the
     *                             property is not considered deprecated. If not {@code null},
     *                             then the property is considered deprecated.
     */
    public record PropertyEntry(
            String name,
            @Nullable String value,
            boolean isPrimary,
            @Nullable String configPropsBeanName,
            @Nullable String description,
            @JsonInclude(JsonInclude.Include.NON_NULL) @Nullable Deprecation deprecation) {}

    /**
     * @param reason       the reason why the given property is deprecated.
     * @param replacement  the name of the property that potentially aims to replace the given deprecated property.
     */
    public record Deprecation(@Nullable String reason, @Nullable String replacement) {}
}
