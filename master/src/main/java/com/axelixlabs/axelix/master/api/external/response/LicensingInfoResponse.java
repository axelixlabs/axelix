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
package com.axelixlabs.axelix.master.api.external.response;

import java.time.Instant;
import java.util.List;

import org.jspecify.annotations.Nullable;

/**
 * Value object that carries the licensing information.
 *
 * @author Mikhail Polivakha
 */
public record LicensingInfoResponse(
        String license,
        @Nullable Instant issuedAt,
        @Nullable Instant validUntil,
        @Nullable String licenseId,
        @Nullable String issuedTo,
        List<Function> functions) {

    public static LicensingInfoResponse oss() {
        return new LicensingInfoResponse("LGPL-3.0", null, null, null, null, ossFunctionsStatus());
    }

    private static List<Function> ossFunctionsStatus() {
        return List.of(
                new Function("Core monitoring", true),
                new Function("Runtime debugging", true),
                new Function("Custom RBAC", false),
                new Function("Large-Scale Activity Monitoring", false),
                new Function("Policy Enforcement", false));
    }

    public record Function(String name, boolean enabled) {}
}
