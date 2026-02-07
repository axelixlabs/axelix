/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.master.api.external.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.api.env.EnvironmentFeed;

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
     * Represents a property value returned by the custom environment endpoint.
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
            @JsonInclude(JsonInclude.Include.NON_NULL) @Nullable Deprecation deprecation,
            @JsonInclude(JsonInclude.Include.NON_NULL) @Nullable List<InjectionPoint> injectionPoints) {}

    /**
     * @param message explaining why the property is deprecated and, optionally, what should be used instead.
     */
    public record Deprecation(String message) {}

    /**
     * InjectionPoint represents a point in the code where a property is injected.
     *
     * @param beanName           the name of the Spring bean where injection occurs.
     * @param injectionType      the type of injection {@link InjectionType}.
     * @param targetName         the target name (field name, method name, or parameter name).
     * @param propertyExpression the property expression used (e.g., "${some.property:default}").
     */
    public record InjectionPoint(
            @JsonProperty("beanName") String beanName,
            @JsonProperty("injectionType") InjectionType injectionType,
            @JsonProperty("targetName") String targetName,
            @JsonProperty("propertyExpression") String propertyExpression) {}

    /**
     * Enumerates the types of injection points where @Value annotations can be applied.
     */
    public enum InjectionType {
        FIELD,
        METHOD,
        CONSTRUCTOR_PARAMETER,
        METHOD_PARAMETER
    }
}
