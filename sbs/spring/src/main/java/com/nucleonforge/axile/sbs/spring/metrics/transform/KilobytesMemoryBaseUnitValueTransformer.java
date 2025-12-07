package com.nucleonforge.axile.sbs.spring.metrics.transform;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.sbs.spring.metrics.transform.units.KiloBytesMemoryBaseUnit;
import com.nucleonforge.axile.sbs.spring.metrics.transform.units.MemoryBaseUnit;

/**
 * {@link AbstractMemoryBaseUnitValueTransformer} for {@link KiloBytesMemoryBaseUnit}.
 *
 * @author Mikhail Polivakha
 */
public class KilobytesMemoryBaseUnitValueTransformer extends AbstractMemoryBaseUnitValueTransformer {

    @NonNull
    @Override
    public MemoryBaseUnit supports() {
        return KiloBytesMemoryBaseUnit.INSTANCE;
    }
}
