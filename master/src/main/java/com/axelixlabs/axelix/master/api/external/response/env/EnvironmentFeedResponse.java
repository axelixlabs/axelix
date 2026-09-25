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
package com.axelixlabs.axelix.master.api.external.response.env;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.contract.env.DeprecationLevel;
import com.axelixlabs.axelix.master.contract.env.InjectionType;

/**
 * The environment properties of a service.
 *
 * @param activeProfiles The active profiles.
 * @param defaultProfiles The default profiles.
 * @param propertySources The property sources in priority order.
 *
 * @author Sergey Cherkasov
 */
public record EnvironmentFeedResponse(
        List<String> activeProfiles, List<String> defaultProfiles, List<PropertySource> propertySources) {

    /**
     * A source of environment properties.
     *
     * @param name The source name.
     * @param description The source description, if any.
     * @param properties The properties in this source.
     *
     * @author Sergey Cherkasov
     */
    public record PropertySource(String name, @Nullable String description, List<Property> properties) {}

    /**
     * A property contributed by a source.
     *
     * @param name The property name.
     * @param value The property value, if shown.
     * @param isPrimary Whether the starter considers this value primary.
     * @param configPropsBeanName The configuration properties bean name, if any.
     * @param description The property description, if any.
     * @param deprecation The deprecation details, if any.
     * @param injectionPoints The injection points, if available.
     * @param dangerousValue The dangerous value details, if any.
     *
     * @author Sergey Cherkasov
     */
    public record Property(
            String name,
            @Nullable String value,
            @JsonProperty("isPrimary") boolean isPrimary,
            @Nullable String configPropsBeanName,
            @Nullable String description,

            @JsonInclude(JsonInclude.Include.NON_NULL) @Nullable
            Deprecation deprecation,

            @Nullable List<InjectionPoint> injectionPoints,

            @JsonInclude(JsonInclude.Include.NON_NULL) @Nullable
            DangerousValue dangerousValue) {}

    /**
     * The deprecation details of a property.
     *
     * @param message The deprecation message.
     * @param level The deprecation severity, if available.
     * @param replacedBy The replacement property, if any.
     *
     * @author Sergey Cherkasov
     */
    public record Deprecation(
            String message,
            @Nullable DeprecationLevel level,
            @Nullable String replacedBy) {}

    /**
     * A point where a property is injected.
     *
     * @param beanName The receiving bean.
     * @param injectionType The injection type.
     * @param targetName The receiving target.
     * @param propertyExpression The property expression.
     *
     * @author Sergey Cherkasov
     */
    public record InjectionPoint(
            String beanName, InjectionType injectionType, String targetName, String propertyExpression) {}

    /**
     * Details of a dangerous property value.
     *
     * @param rationale The reason the value is dangerous.
     * @param alternativeExample A safer example, if available.
     *
     * @author Sergey Cherkasov
     */
    public record DangerousValue(String rationale, @Nullable String alternativeExample) {}
}
