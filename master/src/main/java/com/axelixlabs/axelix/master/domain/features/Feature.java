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
package com.axelixlabs.axelix.master.domain.features;

import com.axelixlabs.axelix.common.utils.SemanticVersion;

/**
 * A single Axelix UI feature whose availability depends on the Axelix starter version a managed service runs. .
 *
 * @param id             the stable, wire-level identifier shared with the front-end (e.g. {@code dependency-analysis}).
 * @param availableSince the earliest Axelix lockstep release the feature is available in, normalised to
 *                       {@code major.minor.0}, since new feature are not introduced in patch versions.
 *
 * @author Mikhail Polivakha
 */
public record Feature(String id, SemanticVersion availableSince) {

    /**
     * @param starterVersion the starter version of a managed instance.
     * @return whether this feature is available for a service running the given starter version.
     */
    public boolean isAvailableFor(SemanticVersion starterVersion) {
        return SemanticVersion.parse(starterVersion.major() + "." + starterVersion.minor() + ".0")
                .isAtLeast(availableSince);
    }
}
