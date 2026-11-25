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
package com.axelixlabs.axelix.master.domain.findings;

import java.util.Objects;

/**
 * Stable metadata for a type of finding. An occurrence supplies its own explanation and remediation.
 */
public record AxelixFinding(FindingId findingId, FindingConcern concern, FindingTechnology technology) {

    public AxelixFinding {
        Objects.requireNonNull(findingId);
        Objects.requireNonNull(concern);
        Objects.requireNonNull(technology);
    }

    public record FindingId(String id) {

        public FindingId {
            if (!id.matches("AXLX-[0-9]{4}")) {
                throw new IllegalArgumentException("Invalid Axelix finding ID: " + id);
            }
        }
    }
}
