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
package com.axelixlabs.axelix.master.domain;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The vendor of the JDK distribution a managed service runs on. Only the most popular vendors are
 * enumerated; anything we cannot recognise is mapped to {@link #UNKNOWN}.
 *
 * <p>The value is resolved from the {@code java.vendor} system property reported by the managed
 * service via {@link #fromVendorName(String)}.
 *
 * @author Mikhail Polivakha
 */
public enum JdkVendor {

    /**
     * Oracle JDK and Oracle OpenJDK builds ({@code java.vendor} = {@code Oracle Corporation}).
     */
    ORACLE("oracle"),

    /**
     * Eclipse Temurin, formerly AdoptOpenJDK ({@code java.vendor} = {@code Eclipse Adoptium}).
     */
    ADOPTIUM("adoptium", "temurin", "adoptopenjdk"),

    /**
     * Amazon Corretto ({@code java.vendor} = {@code Amazon.com Inc.}).
     */
    AMAZON_CORRETTO("amazon", "corretto"),

    /**
     * Azul Zulu ({@code java.vendor} = {@code Azul Systems, Inc.}).
     */
    AZUL_ZULU("azul", "zulu"),

    /**
     * BellSoft Liberica ({@code java.vendor} = {@code BellSoft}).
     */
    BELLSOFT_LIBERICA("bellsoft", "liberica"),

    /**
     * Microsoft Build of OpenJDK ({@code java.vendor} = {@code Microsoft}).
     */
    MICROSOFT("microsoft"),

    /**
     * Red Hat build of OpenJDK ({@code java.vendor} = {@code Red Hat, Inc.}).
     */
    RED_HAT("red hat", "redhat"),

    /**
     * IBM Semeru Runtimes / IBM SDK ({@code java.vendor} = {@code IBM Corporation}).
     */
    IBM_SEMERU("ibm", "international business machines", "semeru"),

    /**
     * SapMachine ({@code java.vendor} = {@code SAP SE}).
     */
    SAP_MACHINE("sap"),

    /**
     * GraalVM ({@code java.vendor} = {@code GraalVM Community}).
     */
    GRAALVM("graalvm"),

    /**
     * Alibaba Dragonwell ({@code java.vendor} = {@code Alibaba}).
     */
    ALIBABA_DRAGONWELL("alibaba", "dragonwell"),

    /**
     * Tencent Kona ({@code java.vendor} = {@code Tencent}).
     */
    TENCENT_KONA("tencent", "kona"),

    /**
     * JetBrains Runtime ({@code java.vendor} = {@code JetBrains s.r.o.}).
     */
    JETBRAINS("jetbrains"),

    /**
     * A vendor we were unable to recognise.
     */
    UNKNOWN();

    private final Set<String> aliases;

    JdkVendor(String... aliases) {
        this.aliases = Arrays.stream(aliases).collect(Collectors.toSet());
    }

    /**
     * Resolves the {@link JdkVendor} from a raw {@code java.vendor} string. Matching is
     * case-insensitive and substring-based, returning {@link #UNKNOWN} when nothing matches.
     *
     * @param vendorName the raw {@code java.vendor} value; may be blank.
     * @return the recognised vendor, or {@link #UNKNOWN}.
     */
    public static JdkVendor fromVendorName(String vendorName) {
        String lowerCase = vendorName.toLowerCase();

        for (JdkVendor value : values()) {
            for (String alias : value.aliases) {
                if (lowerCase.contains(alias)) {
                    return value;
                }
            }
        }

        return JdkVendor.UNKNOWN;
    }
}
