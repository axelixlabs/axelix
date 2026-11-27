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
    AMAZON_CORRETTO("amazon-corretto", VersionFormat.OPENJDK),

    /**
     * Azul Zulu JDK distribution.
     */
    AZUL_ZULU("azul-zulu", VersionFormat.OPENJDK),

    /**
     * BellSoft Liberica JDK distribution.
     */
    BELLSOFT_LIBERICA("bellsoft-liberica", VersionFormat.OPENJDK),

    /**
     * Eclipse Temurin JDK distribution.
     */
    ECLIPSE_TEMURIN("eclipse-temurin", VersionFormat.OPENJDK),

    /**
     * IBM Semeru Runtimes Open Edition.
     */
    IBM_SEMERU("ibm-semeru", VersionFormat.OPENJDK),

    /**
     * Microsoft Build of OpenJDK.
     */
    MICROSOFT_OPENJDK("microsoft-openjdk", VersionFormat.OPENJDK),

    /**
     * Oracle's GPL-licensed OpenJDK distribution.
     */
    ORACLE_OPENJDK("oracle-openjdk", VersionFormat.OPENJDK),

    /**
     * Red Hat build of OpenJDK.
     */
    RED_HAT_OPENJDK("red-hat-openjdk", VersionFormat.OPENJDK),

    /**
     * SapMachine JDK distribution.
     */
    SAP_MACHINE("sapmachine", VersionFormat.OPENJDK),

    /**
     * GraalVM Community Edition JDK distribution.
     */
    GRAALVM("graalvm-ce", VersionFormat.OPENJDK),

    /**
     * Alibaba Dragonwell JDK distribution.
     */
    ALIBABA_DRAGONWELL("alibaba-dragonwell", VersionFormat.OPENJDK),

    /**
     * Tencent Kona JDK distribution.
     */
    TENCENT_KONA("tencent-kona", VersionFormat.OPENJDK),

    /**
     * JetBrains Runtime JDK distribution.
     */
    JETBRAINS("jetbrains-runtime", VersionFormat.OPENJDK),

    /**
     * Spring Boot as the Platform.
     */
    SPRING_BOOT("spring-boot", VersionFormat.SPRING),

    /**
     * Spring Framework as the Platform.
     */
    SPRING_FRAMEWORK("spring-framework", VersionFormat.SPRING);

    enum VersionFormat {
        OPENJDK,
        SPRING
    }

    private final String codeName;
    private final VersionFormat versionFormat;

    PlatformName(String codeName, VersionFormat versionFormat) {
        this.codeName = codeName;
        this.versionFormat = versionFormat;
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

    VersionFormat versionFormat() {
        return versionFormat;
    }

    public String codeName() {
        return codeName;
    }
}
