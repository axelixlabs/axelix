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
package com.axelixlabs.axelix.common.api.env;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * The response to axelix-env actuator endpoint.
 *
 * @since 26.08.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public final class EnvironmentFeed {

    private final List<String> activeProfiles;
    private final List<String> defaultProfiles;
    private final List<PropertySource> propertySources;

    /**
     * Creates a new EnvironmentFeed.
     *
     * @param activeProfiles   the list of currently active Spring profiles.
     * @param defaultProfiles  the list of default Spring profiles.
     * @param propertySources  the list of property sources with their short profiles.
     */
    @JsonCreator
    public EnvironmentFeed(
            @JsonProperty("activeProfiles") List<String> activeProfiles,
            @JsonProperty("defaultProfiles") List<String> defaultProfiles,
            @JsonProperty("propertySources") List<PropertySource> propertySources) {
        this.activeProfiles = activeProfiles;
        this.defaultProfiles = defaultProfiles;
        this.propertySources = propertySources;
    }

    public List<String> getActiveProfiles() {
        return activeProfiles;
    }

    public List<String> getDefaultProfiles() {
        return defaultProfiles;
    }

    public List<PropertySource> getPropertySources() {
        return propertySources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EnvironmentFeed that = (EnvironmentFeed) o;
        return Objects.equals(activeProfiles, that.activeProfiles)
                && Objects.equals(defaultProfiles, that.defaultProfiles)
                && Objects.equals(propertySources, that.propertySources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activeProfiles, defaultProfiles, propertySources);
    }

    @Override
    public String toString() {
        return "EnvironmentFeed{"
                + "activeProfiles="
                + activeProfiles
                + ", defaultProfiles="
                + defaultProfiles
                + ", propertySources="
                + propertySources
                + '}';
    }

    /**
     * DTO that encapsulates the property source of the given artifact.
     */
    public static final class PropertySource {

        private final String sourceName;

        @Nullable
        private final String sourceDescription;

        private final List<Property> properties;

        /**
         * Creates a new PropertySource.
         *
         * @param sourceName        the sourceName of the property source.
         * @param sourceDescription the custom description of this property source, if any.
         * @param properties        the list of property entries.
         */
        @JsonCreator
        public PropertySource(
                @JsonProperty("sourceName") String sourceName,
                @JsonProperty("sourceDescription") @Nullable String sourceDescription,
                @JsonProperty("properties") List<Property> properties) {
            this.sourceName = sourceName;
            this.sourceDescription = sourceDescription;
            this.properties = properties;
        }

        public String getSourceName() {
            return sourceName;
        }

        @Nullable
        public String getSourceDescription() {
            return sourceDescription;
        }

        public List<Property> getProperties() {
            return properties;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PropertySource that = (PropertySource) o;
            return Objects.equals(sourceName, that.sourceName)
                    && Objects.equals(sourceDescription, that.sourceDescription)
                    && Objects.equals(properties, that.properties);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourceName, sourceDescription, properties);
        }

        @Override
        public String toString() {
            return "PropertySource{"
                    + "sourceName='"
                    + sourceName
                    + '\''
                    + ", sourceDescription='"
                    + sourceDescription
                    + '\''
                    + ", properties="
                    + properties
                    + '}';
        }
    }

    /**
     * DTO representing a property value returned by the custom actuator environment endpoint.
     */
    public static final class Property {

        private final String propertyName;

        @Nullable
        private final String value;

        @JsonProperty("isPrimary")
        private final boolean isPrimary;

        @Nullable
        private final String configPropsBeanName;

        @Nullable
        private final String description;

        @Nullable
        private final Deprecation deprecation;

        @Nullable
        private final List<InjectionPoint> injectionPoints;

        /**
         * Creates a new Property.
         *
         * @param propertyName        the property name.
         * @param value               the string representation of the property's value.
         * @param isPrimary           whether this property value is primary (i.e. this value takes precedence over the other values
         *                            from other property sources).
         * @param configPropsBeanName the propertyName of the configProps (if any) bean onto which this property maps,
         *                            {@code null} otherwise.
         * @param description         the description from spring-configuration-metadata.json
         * @param deprecation         deprecation related information. If {@code null}, the
         *                            property is not considered deprecated. If not {@code null},
         *                            then the property is considered deprecated.
         * @param injectionPoints     the injection points where this property is used.
         */
        @JsonCreator
        public Property(
                @JsonProperty("propertyName") String propertyName,
                @JsonProperty("value") @Nullable String value,
                @JsonProperty("isPrimary") boolean isPrimary,
                @JsonProperty("configPropsBeanName") @Nullable String configPropsBeanName,
                @JsonProperty("description") @Nullable String description,
                @JsonProperty("deprecation") @Nullable Deprecation deprecation,
                @JsonProperty("injectionPoints") @Nullable List<InjectionPoint> injectionPoints) {
            this.propertyName = propertyName;
            this.value = value;
            this.isPrimary = isPrimary;
            this.configPropsBeanName = configPropsBeanName;
            this.description = description;
            this.deprecation = deprecation;
            this.injectionPoints = injectionPoints;
        }

        public String getPropertyName() {
            return propertyName;
        }

        @Nullable
        public String getValue() {
            return value;
        }

        public boolean isPrimary() {
            return isPrimary;
        }

        @Nullable
        public String getConfigPropsBeanName() {
            return configPropsBeanName;
        }

        @Nullable
        public String getDescription() {
            return description;
        }

        @Nullable
        public Deprecation getDeprecation() {
            return deprecation;
        }

        @Nullable
        public List<InjectionPoint> getInjectionPoints() {
            return injectionPoints;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Property property = (Property) o;
            return isPrimary == property.isPrimary
                    && Objects.equals(propertyName, property.propertyName)
                    && Objects.equals(value, property.value)
                    && Objects.equals(configPropsBeanName, property.configPropsBeanName)
                    && Objects.equals(description, property.description)
                    && Objects.equals(deprecation, property.deprecation)
                    && Objects.equals(injectionPoints, property.injectionPoints);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    propertyName, value, isPrimary, configPropsBeanName, description, deprecation, injectionPoints);
        }

        @Override
        public String toString() {
            return "Property{"
                    + "propertyName='"
                    + propertyName
                    + '\''
                    + ", value='"
                    + value
                    + '\''
                    + ", isPrimary="
                    + isPrimary
                    + ", configPropsBeanName='"
                    + configPropsBeanName
                    + '\''
                    + ", description='"
                    + description
                    + '\''
                    + ", deprecation="
                    + deprecation
                    + ", injectionPoints="
                    + injectionPoints
                    + '}';
        }
    }

    /**
     * DTO that encapsulates the deprecation property of the given artifact.
     */
    public static final class Deprecation {

        private final String message;

        /**
         * Creates a new Deprecation.
         *
         * @param message explaining why the property is deprecated and, optionally, what should be used instead.
         */
        @JsonCreator
        public Deprecation(@JsonProperty("message") String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Deprecation that = (Deprecation) o;
            return Objects.equals(message, that.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(message);
        }

        @Override
        public String toString() {
            return "Deprecation{" + "message='" + message + '\'' + '}';
        }
    }

    /**
     * InjectionPoint represents a point in the code where a property is injected.
     */
    public static final class InjectionPoint {

        private final String beanName;
        private final InjectionType injectionType;
        private final String targetName;
        private final String propertyExpression;

        /**
         * Creates a new InjectionPoint.
         *
         * @param beanName           the name of the Spring bean where injection occurs.
         * @param injectionType      the type of injection {@link InjectionType}.
         * @param targetName         the target name (field name, method name, or parameter name).
         * @param propertyExpression the property expression used (e.g., "${some.property:default}").
         */
        @JsonCreator
        public InjectionPoint(
                @JsonProperty("beanName") String beanName,
                @JsonProperty("injectionType") InjectionType injectionType,
                @JsonProperty("targetName") String targetName,
                @JsonProperty("propertyExpression") String propertyExpression) {
            this.beanName = beanName;
            this.injectionType = injectionType;
            this.targetName = targetName;
            this.propertyExpression = propertyExpression;
        }

        public String getBeanName() {
            return beanName;
        }

        public InjectionType getInjectionType() {
            return injectionType;
        }

        public String getTargetName() {
            return targetName;
        }

        public String getPropertyExpression() {
            return propertyExpression;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            InjectionPoint that = (InjectionPoint) o;
            return Objects.equals(beanName, that.beanName)
                    && injectionType == that.injectionType
                    && Objects.equals(targetName, that.targetName)
                    && Objects.equals(propertyExpression, that.propertyExpression);
        }

        @Override
        public int hashCode() {
            return Objects.hash(beanName, injectionType, targetName, propertyExpression);
        }

        @Override
        public String toString() {
            return "InjectionPoint{"
                    + "beanName='"
                    + beanName
                    + '\''
                    + ", injectionType="
                    + injectionType
                    + ", targetName='"
                    + targetName
                    + '\''
                    + ", propertyExpression='"
                    + propertyExpression
                    + '\''
                    + '}';
        }
    }

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
