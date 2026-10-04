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

import java.util.Set;

/**
 * The vendor of the JDK distribution a managed service runs on. Only the most popular vendors are
 * enumerated; anything we cannot recognize is mapped to {@link #UNKNOWN}.
 *
 * @author Mikhail Polivakha
 */
public enum JdkVendor {

    /**
     * Oracle JDK and Oracle OpenJDK builds ({@code java.vendor} = {@code Oracle Corporation}).
     */
    ORACLE("Oracle", Set.of("oracle")),

    /**
     * Eclipse Temurin, formerly AdoptOpenJDK ({@code java.vendor} = {@code Eclipse Adoptium}).
     */
    ADOPTIUM("Eclipse Temurin", Set.of("adoptium", "temurin", "adoptopenjdk")),

    /**
     * Amazon Corretto ({@code java.vendor} = {@code Amazon.com Inc.}).
     */
    AMAZON_CORRETTO("Amazon Corretto", Set.of("amazon", "corretto")),

    /**
     * Azul Zulu ({@code java.vendor} = {@code Azul Systems, Inc.}).
     */
    AZUL_ZULU("Azul Zulu", Set.of("azul", "zulu")),

    /**
     * BellSoft Liberica ({@code java.vendor} = {@code BellSoft}).
     */
    BELLSOFT_LIBERICA("BellSoft Liberica", Set.of("bellsoft", "liberica")),

    /**
     * Microsoft Build of OpenJDK ({@code java.vendor} = {@code Microsoft}).
     */
    MICROSOFT("Microsoft Build of OpenJDK", Set.of("microsoft")),

    /**
     * Red Hat build of OpenJDK ({@code java.vendor} = {@code Red Hat, Inc.}).
     */
    RED_HAT("Red Hat", Set.of("red hat", "redhat")),

    /**
     * IBM Semeru Runtimes / IBM SDK ({@code java.vendor} = {@code IBM Corporation}).
     */
    IBM_SEMERU("IBM Semeru", Set.of("ibm", "international business machines", "semeru")),

    /**
     * SapMachine ({@code java.vendor} = {@code SAP SE}).
     */
    SAP_MACHINE("SapMachine", Set.of("sap")),

    /**
     * GraalVM ({@code java.vendor} = {@code GraalVM Community}).
     */
    GRAALVM("GraalVM", Set.of("graalvm")),

    /**
     * Alibaba Dragonwell ({@code java.vendor} = {@code Alibaba}).
     */
    ALIBABA_DRAGONWELL("Alibaba Dragonwell", Set.of("alibaba", "dragonwell")),

    /**
     * Tencent Kona ({@code java.vendor} = {@code Tencent}).
     */
    TENCENT_KONA("Tencent Kona", Set.of("tencent", "kona")),

    /**
     * JetBrains Runtime ({@code java.vendor} = {@code JetBrains s.r.o.}).
     */
    JETBRAINS("JetBrains Runtime", Set.of("jetbrains")),

    /**
     * A vendor we were unable to recognise.
     */
    UNKNOWN("Unknown", Set.of());

    private final String displayName;

    private final Set<String> aliases;

    JdkVendor(String displayName, Set<String> aliases) {
        this.displayName = displayName;
        this.aliases = aliases;
    }

    /**
     * @return the human-readable name of this vendor, suitable for display in the UI.
     */
    public String getDisplayName() {
        return displayName;
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
