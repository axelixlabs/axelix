package com.nucleonforge.axile.sbs.spring.metrics.transform.units;

import java.util.Set;

/**
 * Bytes {@link MemoryBaseUnit}.
 *
 * @author Mikhail Polivakha
 */
public class BytesMemoryBaseUnit extends MemoryBaseUnit {

    public static final BytesMemoryBaseUnit INSTANCE = new BytesMemoryBaseUnit(Set.of("bytes"), "bytes");

    public BytesMemoryBaseUnit(Set<String> aliases, String displayName) {
        super(aliases, displayName);
    }

    @Override
    public MemoryBaseUnit next() {
        return KiloBytesMemoryBaseUnit.INSTANCE;
    }
}
