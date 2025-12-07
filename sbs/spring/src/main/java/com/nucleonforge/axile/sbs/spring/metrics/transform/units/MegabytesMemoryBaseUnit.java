package com.nucleonforge.axile.sbs.spring.metrics.transform.units;

import java.util.Set;

import org.jspecify.annotations.Nullable;

/**
 * Megabytes {@link MemoryBaseUnit}.
 *
 * @author Mikhail Polivakha
 */
public class MegabytesMemoryBaseUnit extends MemoryBaseUnit {

    public static final MegabytesMemoryBaseUnit INSTANCE = new MegabytesMemoryBaseUnit(Set.of("megabytes"), "MB");

    public MegabytesMemoryBaseUnit(Set<String> aliases, String displayName) {
        super(aliases, displayName);
    }

    @Nullable
    @Override
    public MemoryBaseUnit next() {
        return null;
    }
}
