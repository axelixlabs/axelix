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

/**
 * Injection type of the given property, that is to say - information on
 * how exactly this property is injected into the given bean.
 */
export enum EPropertyInjectionType {
    FIELD = "FIELD",
    METHOD = "METHOD",
    CONSTRUCTOR_PARAMETER = "CONSTRUCTOR_PARAMETER",
    METHOD_PARAMETER = "METHOD_PARAMETER",
}

/**
 * The severity with which a property deprecation is reported by the Spring Boot configuration
 * metadata.
 */
export enum EDeprecationLevel {
    /**
     * The property is still bound, but it is slated for removal in a future release.
     */
    WARNING = "WARNING",

    /**
     * The property is no longer bound, so its value has no effect and startup breaks once strict
     * binding is enabled.
     */
    ERROR = "ERROR",
}

/**
 * The categories a property can be triaged into. They are the facets the user can filter the
 * property list down to, so that the handful of properties that need attention do not have to be
 * spotted by eye among the hundreds that do not.
 */
export enum EPropertyTriageTag {
    /**
     * The property carries a value the master flagged as dangerous (e.g. one that exposes internal
     * state or lets the schema drift at runtime).
     */
    DANGEROUS = "DANGEROUS",

    /**
     * The property is deprecated with {@link EDeprecationLevel.ERROR} severity.
     */
    DEPRECATED_ERROR = "DEPRECATED_ERROR",

    /**
     * The property is deprecated with {@link EDeprecationLevel.WARNING} severity.
     */
    DEPRECATED_WARNING = "DEPRECATED_WARNING",

    /**
     * The property is defined in this property source, but a source of a higher precedence
     * defines it as well, so this occurrence never takes effect.
     */
    SUPPRESSED = "SUPPRESSED",
}
