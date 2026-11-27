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
 * JDK distributions have distinct platform names because vendors publish their own support schedules.
 *
 * @author Mikhail Polivakha
 */
public enum PlatformName {
    /**
     * Amazon Corretto JDK distribution.
     */
    AMAZON_CORRETTO("amazon-corretto"),

    /**
     * BellSoft Liberica JDK distribution.
     */
    BELLSOFT_LIBERICA("bellsoft-liberica"),

    /**
     * Eclipse Temurin JDK distribution.
     */
    ECLIPSE_TEMURIN("eclipse-temurin"),

    /**
     * Oracle's GPL-licensed OpenJDK distribution.
     */
    ORACLE_OPENJDK("oracle-openjdk"),

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
