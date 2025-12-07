package com.nucleonforge.axile.sbs.spring.metrics.transform.units;

import java.util.Set;

/**
 * Kilobytes {@link MemoryBaseUnit}.
 *
 * @author Mikhail Polivakha
 */
public class KiloBytesMemoryBaseUnit extends MemoryBaseUnit {

    public static final KiloBytesMemoryBaseUnit INSTANCE = new KiloBytesMemoryBaseUnit(Set.of("kilobytes"), "KB");

    public KiloBytesMemoryBaseUnit(Set<String> aliases, String displayName) {
        super(aliases, displayName);
    }

    @Override
    public MemoryBaseUnit next() {
        return MegabytesMemoryBaseUnit.INSTANCE;
    }
}
