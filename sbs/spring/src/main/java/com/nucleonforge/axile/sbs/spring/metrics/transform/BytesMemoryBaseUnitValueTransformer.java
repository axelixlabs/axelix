package com.nucleonforge.axile.sbs.spring.metrics.transform;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.sbs.spring.metrics.transform.units.BytesMemoryBaseUnit;
import com.nucleonforge.axile.sbs.spring.metrics.transform.units.MemoryBaseUnit;

/**
 * {@link AbstractMemoryBaseUnitValueTransformer} for {@link BytesMemoryBaseUnit}.
 *
 * @author Mikhail Polivakha
 */
public class BytesMemoryBaseUnitValueTransformer extends AbstractMemoryBaseUnitValueTransformer {

    @NonNull
    @Override
    public MemoryBaseUnit supports() {
        return BytesMemoryBaseUnit.INSTANCE;
    }
}
