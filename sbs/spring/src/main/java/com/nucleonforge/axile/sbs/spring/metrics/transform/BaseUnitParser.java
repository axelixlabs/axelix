/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.sbs.spring.metrics.transform;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.sbs.spring.metrics.transform.units.BaseUnit;
import com.nucleonforge.axile.sbs.spring.metrics.transform.units.BytesMemoryBaseUnit;
import com.nucleonforge.axile.sbs.spring.metrics.transform.units.KiloBytesMemoryBaseUnit;
import com.nucleonforge.axile.sbs.spring.metrics.transform.units.MegabytesMemoryBaseUnit;

/**
 * Parser that is capable to parse the given source string into the {@link BaseUnit}.
 *
 * @author Mikhail Polivakha
 */
public class BaseUnitParser {

    private static final Set<BaseUnit> UNITS =
            Set.of(BytesMemoryBaseUnit.INSTANCE, KiloBytesMemoryBaseUnit.INSTANCE, MegabytesMemoryBaseUnit.INSTANCE);

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
