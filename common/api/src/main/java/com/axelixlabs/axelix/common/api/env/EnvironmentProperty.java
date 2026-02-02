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

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * The response to env/property/{propertyName} actuator endpoint.
 *
 * @since 02.09.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public final class EnvironmentProperty {

    private final Property property;
    private final List<String> activeProfiles;
    private final List<String> defaultProfiles;
    private final List<SourceEntry> propertySources;

    /**
     * Creates a new EnvironmentProperty.
     *
     * @param property        The resolved property with its value and source.
     * @param activeProfiles  The currently active Spring Boot application profiles, not specific to this property.
     * @param defaultProfiles The default Spring Boot application profiles, not specific to this property.
     * @param propertySources The property sources that contributed to resolving this property.
     */
    public EnvironmentProperty(
            @JsonProperty("property") Property property,
            @JsonProperty("activeProfiles") List<String> activeProfiles,
            @JsonProperty("defaultProfiles") List<String> defaultProfiles,
            @JsonProperty("propertySources") List<SourceEntry> propertySources) {
        this.property = property;
        this.activeProfiles = activeProfiles;
        this.defaultProfiles = defaultProfiles;
        this.propertySources = propertySources;
    }

    public Property property() {
        return property;
    }

    public List<String> activeProfiles() {
        return activeProfiles;
    }

    public List<String> defaultProfiles() {
        return defaultProfiles;
    }

    public List<SourceEntry> propertySources() {
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
        EnvironmentProperty that = (EnvironmentProperty) o;
        return Objects.equals(property, that.property)
                && Objects.equals(activeProfiles, that.activeProfiles)
                && Objects.equals(defaultProfiles, that.defaultProfiles)
                && Objects.equals(propertySources, that.propertySources);
    }

    @Override
    public int hashCode() {
        return Objects.hash(property, activeProfiles, defaultProfiles, propertySources);
    }

    @Override
    public String toString() {
        return "EnvironmentProperty{"
                + "property="
                + property
                + ", activeProfiles="
                + activeProfiles
                + ", defaultProfiles="
                + defaultProfiles
                + ", propertySources="
                + propertySources
                + '}';
    }

    public static final class Property {

        private final String source;
        private final String value;

        public Property(@JsonProperty("source") String source, @JsonProperty("value") String value) {
            this.source = source;
            this.value = value;
        }

        public String source() {
            return source;
        }

        public String value() {
            return value;
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
            return Objects.equals(source, property.source) && Objects.equals(value, property.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(source, value);
        }

        @Override
        public String toString() {
            return "Property{" + "source='" + source + '\'' + ", value='" + value + '\'' + '}';
        }
    }

    public static final class SourceEntry {

        private final String sourceName;

        @Nullable
        private final PropertyValue property;

        public SourceEntry(
                @JsonProperty("name") String sourceName, @JsonProperty("property") @Nullable PropertyValue property) {
            this.sourceName = sourceName;
            this.property = property;
        }

        public String sourceName() {
            return sourceName;
        }

        @Nullable
        public PropertyValue property() {
            return property;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SourceEntry that = (SourceEntry) o;
            return Objects.equals(sourceName, that.sourceName) && Objects.equals(property, that.property);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sourceName, property);
        }

        @Override
        public String toString() {
            return "SourceEntry{" + "sourceName='" + sourceName + '\'' + ", property=" + property + '}';
        }
    }
}
