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

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.api.transform.units.BaseUnit;
import com.axelixlabs.axelix.common.api.transform.units.BytesMemoryBaseUnit;
import com.axelixlabs.axelix.common.api.transform.units.GigabytesMemoryBaseUnit;
import com.axelixlabs.axelix.common.api.transform.units.KilobytesMemoryBaseUnit;
import com.axelixlabs.axelix.common.api.transform.units.MegabytesMemoryBaseUnit;

/**
 * Parser that is capable to parse the given source string into the {@link BaseUnit}.
 *
 * @author Mikhail Polivakha
 */
public class BaseUnitParser {

    private static final Set<BaseUnit> UNITS = Set.of(
            BytesMemoryBaseUnit.INSTANCE,
            KilobytesMemoryBaseUnit.INSTANCE,
            MegabytesMemoryBaseUnit.INSTANCE,
            GigabytesMemoryBaseUnit.INSTANCE);

    /**
     * Cache that holds computed {@link BaseUnit} for the given source string.
     */
    private static final ConcurrentMap<String, Optional<BaseUnit>> cache = new ConcurrentHashMap<>();

    /**
     * @return the base unit which input source represents, or {@link Optional#empty()}
     *         if the input source does not represent any {@link BaseUnit}.
     */
    public Optional<BaseUnit> parse(@Nullable String source) {
        if (source == null) {
            return Optional.empty();
        }

        return cache.compute(source, (input, memoryBaseUnit) -> {
            String lowerCase = input.toLowerCase();

            for (BaseUnit value : UNITS) {
                for (String alias : value.getAliases()) {
                    if (lowerCase.equals(alias)) {
                        return Optional.of(value);
                    }
                }
            }

            return Optional.empty();
        });
    }
}
