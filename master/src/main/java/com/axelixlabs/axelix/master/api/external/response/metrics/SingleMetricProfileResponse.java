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
package com.axelixlabs.axelix.master.api.external.response.metrics;

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * Object that encapsulates the profile of the given metric.
 *
 * @author Mikhail Polivakha
 */
public record SingleMetricProfileResponse(
        String name,
        @Nullable String description,
        String baseUnit,
        List<Measurement> measurements,
        List<Map<String, String>> validTagCombinations) {

    /**
     * Single metric value, measured at a particular point in time.
     *
     * @param value the value of the given metric.
     */
    public record Measurement(double value) {}
}
