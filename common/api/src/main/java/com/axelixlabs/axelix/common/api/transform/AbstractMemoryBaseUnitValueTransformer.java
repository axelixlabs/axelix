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
package com.axelixlabs.axelix.common.api.transform;

import org.jspecify.annotations.NonNull;

import com.axelixlabs.axelix.common.api.transform.units.MemoryBaseUnit;

/**
 * Abstract {@link BaseUnitValueTransformer} for memory-related base units.
 *
 * @author Mikhail Polivakha
 */
public abstract class AbstractMemoryBaseUnitValueTransformer implements BaseUnitValueTransformer {

    /**
     * The memory multiplier. Essentially, the multiplier that is required
     * to turn 1 KB into 1 MB, 1 MB into 1 GB and so on.
     */
    private static final int MEMORY_MULTIPLIER = 1024;

    @Override
    public TransformedMetricValue transform(double value) {
        MemoryBaseUnit newBaseUnit = supports();

        double result = value;

        while (result > MEMORY_MULTIPLIER && newBaseUnit.next() != null) {
            newBaseUnit = newBaseUnit.next();
            result /= MEMORY_MULTIPLIER;
        }

        return new TransformedMetricValue(newBaseUnit, Math.round(result * 100) / 100.0);
    }

    @NonNull
    @Override
    public abstract MemoryBaseUnit supports();
}
