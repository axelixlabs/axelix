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
