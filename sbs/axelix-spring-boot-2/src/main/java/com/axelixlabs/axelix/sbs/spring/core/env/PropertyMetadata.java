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
package com.axelixlabs.axelix.sbs.spring.core.env;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * Metadata for a Spring Boot property, including description and deprecation info.
 *
 * @since 04.12.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public final class PropertyMetadata {
    @Nullable
    private final String description;

    @Nullable
    private final Deprecation deprecation;

    /**
     * @param description the property description.
     * @param deprecation deprecation related information. If {@code null}, the
     *                    property is not considered deprecated. If not {@code null},
     *                    then the property is considered deprecated.
     */
    public PropertyMetadata(@Nullable String description, @Nullable Deprecation deprecation) {
        this.description = description;
        this.deprecation = deprecation;
    }

    @Nullable
    public String description() {
        return description;
    }

    @Nullable
    public Deprecation deprecation() {
        return deprecation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PropertyMetadata) obj;
        return Objects.equals(this.description, that.description) && Objects.equals(this.deprecation, that.deprecation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, deprecation);
    }

    @Override
    public String toString() {
        return "PropertyMetadata[" + "description=" + description + ", " + "deprecation=" + deprecation + ']';
    }

    /**
     * Deprecation metadata for a property.
     */
    public static final class Deprecation {

        private final String message;

        /**
         * @param message explaining why the property is deprecated and, optionally, what should be used instead.
         */
        public Deprecation(String message) {
            this.message = message;
        }

        public String message() {
            return message;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (Deprecation) obj;
            return Objects.equals(this.message, that.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(message);
        }

        @Override
        public String toString() {
            return "Deprecation[" + "message=" + message + ']';
        }
    }
}
