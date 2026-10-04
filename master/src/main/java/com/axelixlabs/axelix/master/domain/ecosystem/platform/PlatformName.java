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
package com.axelixlabs.axelix.master.domain.ecosystem.platform;

/**
 * The {@link Platform Platforms} that are supported and known to Axelix Master.
 * <p>
 * <strong>Important Note!</strong> It is vital to distinguish the platform from the vendor of the given Platform. The
 * Platform itself is independent of the vendor.
 * <p>
 * For example, OpenJDK is the platform on which de-facto every java deployment is running, but there may be multiple
 * potential vendors of the OpenJDK.
 * <p>
 * Same about Spring Boot. Spring Boot is fundamental particle, the fundamental platform for all microservices. Yet,
 * we must realize that the <strong>BUILD</strong> of the Spring Boot is provided by the vendor. Sure, the build may be the OSS
 * build, where we may colloquially call "OSS" as the vendor, but there are other potential builds of the Spring Boot platform
 * provided by:
 *
 * <ol>
 *     <li>1. Broadcom</li>
 *     <li>2. Herodevs</li>
 *     <li>3. Tuxcare</li>
 * </ol>
 *
 * And so on. These are vendors of the Spring Boot.
 *
 * @author Mikhail Polivakha
 */
public enum PlatformName {
    /**
     * OpenJDK as the Platform.
     */
    OPEN_JDK("openjdk"),

    /**
     * Spring Boot as the Platform.
     */
    SPRING_BOOT("spring-boot"),

    /**
     * Spring Framework as the Platform.
     */
    SPRING_FRAMEWORK("spring-framework");

    private final String codeName;

    PlatformName(String codeName) {
        this.codeName = codeName;
    }

    public static PlatformName valueOfElseThrow(String platform) {
        for (PlatformName value : values()) {
            if (value.codeName().equals(platform)) {
                return value;
            }
        }

        throw new IllegalArgumentException(
                "Unrecognized framework provided in the framework manifest file: " + platform);
    }

    public String codeName() {
        return codeName;
    }
}
