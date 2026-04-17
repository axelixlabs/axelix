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
package com.axelixlabs.axelix.sbs.spring.core.master;

import org.jspecify.annotations.Nullable;

import static com.axelixlabs.axelix.sbs.spring.core.utils.StringUtils.emptyIfNull;

/**
 * Provides runtime and framework version information that is attached to service metadata
 * reported by the starter.
 *
 * @author Sergey Cherkasov
 */
public interface LibraryInformationProvider {

    /**
     * Returns the Kotlin runtime version when Kotlin is present on the application classpath.
     *
     * @return current Kotlin version, or {@code null} when Kotlin classes are unavailable
     */
    @Nullable
    default String getKotlinVersion() {
        return null;
    }

    /**
     * Returns the Java runtime version.
     *
     * @return value of {@code java.version}, or an empty string when the property is absent
     */
    default String getJavaVersion() {
        return emptyIfNull(System.getProperty("java.version"));
    }

    /**
     * Returns the JDK vendor name.
     *
     * @return value of {@code java.vendor}, or an empty string when the property is absent
     */
    default String getJdkVendorName() {
        return emptyIfNull(System.getProperty("java.vendor"));
    }

    /**
     * Returns the JDK vendor version.
     *
     * @return value of {@code java.vendor.version}, or an empty string when the property is absent
     */
    default String getJdkVendorVersion() {
        return emptyIfNull(System.getProperty("java.vendor.version"));
    }

    /**
     * Returns the Spring Boot version.
     *
     * @return Spring Boot version, or an empty string when it is not available
     */
    default String getSpringBootVersion() {
        return "";
    }

    /**
     * Returns the Spring Framework version.
     *
     * @return Spring Framework version, or an empty string when it is not available
     */
    default String getSpringVersion() {
        return "";
    }

    // TODO Currently we cannot get the current Spring Cloud version like we do for Spring/Spring Boot
    // "SpringBootVersion.getVersion()". We hope this capability will be available in the future.
    // https://github.com/spring-cloud/spring-cloud-release/issues/451
    // We also have our own ideas for how to solve this problem https://github.com/axelixlabs/axelix/issues/935
    /**
     * Returns the Spring Cloud version.
     *
     * @return Spring Cloud version, or {@code null} when it is not available
     */
    @Nullable
    default String getSpringCloudVersion() {
        return null;
    }
}
