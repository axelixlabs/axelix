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
package com.axelixlabs.axelix.master.api.external.response.software;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.NonNull;

/**
 * Response that contains the summary of used versions for the
 * <strong>single software component</strong> used in the ecosystem.
 *
 * @author Mikhail Polivakha
 */
public class DistributionResponse {

    private final String softwareComponentName;

    private final Map<String, Long> versions;

    /**
     * @param name the name of the software component used in the ecosystem.
     */
    public DistributionResponse(@NonNull String name) {
        this.versions = new HashMap<>();
        this.softwareComponentName = name;
    }

    /**
     * Increase the versions counter for the given {@link #softwareComponentName}.
     */
    public void addVersion(String version) {
        versions.compute(version, (key, value) -> value == null ? 1L : value + 1);
    }

    public String getSoftwareComponentName() {
        return softwareComponentName;
    }

    public Map<String, Long> getVersions() {
        return versions;
    }
}
