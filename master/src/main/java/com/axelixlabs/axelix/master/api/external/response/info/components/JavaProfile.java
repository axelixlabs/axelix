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
package com.axelixlabs.axelix.master.api.external.response.info.components;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

/**
 * The profile of a given Java environment.
 *
 * @param version     The version of the Java.
 * @param vendor      The vendor details of the Java, if available.
 * @param runtime     The runtime details of the Java, if available.
 * @param jvm         The JVM details of the Java, if available.
 *
 * @author Sergey Cherkasov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record JavaProfile(
        String version,
        @Nullable Vendor vendor,
        @Nullable Runtime runtime,
        @Nullable JVM jvm) {
    /**
     * The profile of a given vendor.
     *
     * @param name     The name of the vendor.
     * @param version  The version of the vendor.
     *
     * @author Sergey Cherkasov
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Vendor(String name, String version) {}

    /**
     * The profile of a given runtime.
     *
     * @param name     The name of the runtime.
     * @param version  The version of the runtime.
     *
     * @author Sergey Cherkasov
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Runtime(String name, String version) {}

    /**
     * The profile of a given JVM.
     *
     * @param name     The name of the JVM.
     * @param vendor   The vendor of the JVM
     * @param version  The version of the JVM.
     *
     * @author Sergey Cherkasov
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record JVM(String name, String vendor, String version) {}
}
