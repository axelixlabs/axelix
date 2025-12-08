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

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.sbs.spring.metrics.transform.units.MemoryBaseUnit;

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
