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
package com.axelixlabs.axelix.common.api.registration.insights.persistence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionOverallStats {

    private final long minMs;
    private final long maxMs;
    private final long averageMs;

    @JsonCreator
    public TransactionOverallStats(
            @JsonProperty("minMs") long minMs,
            @JsonProperty("maxMs") long max,
            @JsonProperty("averageMs") long averageMs) {
        this.minMs = minMs;
        this.maxMs = max;
        this.averageMs = averageMs;
    }

    public long getMinMs() {
        return minMs;
    }

    public long getMaxMs() {
        return maxMs;
    }

    public long getAverageMs() {
        return averageMs;
    }
}
