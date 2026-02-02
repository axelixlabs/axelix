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
package com.axelixlabs.axelix.common.api.registration;

import java.util.Objects;

/**
 * Short information about the build of the given service. Provided during initial scan.
 *
 * @author Mikhail Polivakha
 */
public final class ShortBuildInfo {

    private final String buildTimestamp;
    private final String serviceVersion;

    /**
     * Creates a new ShortBuildInfo.
     *
     * @param buildTimestamp the timestamp when this application's build was created
     * @param serviceVersion the version of the <strong>managed service itself</strong>, i.e. the version
     *                       of the end-service artifact (the V inside GAV coordinate). The assumption is that
     *                       is never {@code null}, and it frankly should not be.
     */
    public ShortBuildInfo(String buildTimestamp, String serviceVersion) {
        this.buildTimestamp = buildTimestamp;
        this.serviceVersion = serviceVersion;
    }

    public String buildTimestamp() {
        return buildTimestamp;
    }

    public String serviceVersion() {
        return serviceVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ShortBuildInfo that = (ShortBuildInfo) o;
        return Objects.equals(buildTimestamp, that.buildTimestamp)
                && Objects.equals(serviceVersion, that.serviceVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buildTimestamp, serviceVersion);
    }

    @Override
    public String toString() {
        return "ShortBuildInfo{"
                + "buildTimestamp='"
                + buildTimestamp
                + '\''
                + ", serviceVersion='"
                + serviceVersion
                + '\''
                + '}';
    }
}
