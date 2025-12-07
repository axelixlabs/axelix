package com.nucleonforge.axile.sbs.spring.metrics.transform.units;

import java.util.Set;

import org.jspecify.annotations.Nullable;

/**
 * {@link AbstractBaseUnit} of Memory. The memory is structured.
 *
 * @author Mikhail Polivakha
 */
public abstract class MemoryBaseUnit extends AbstractBaseUnit {

    public MemoryBaseUnit(Set<String> aliases, String displayName) {
        super(aliases, displayName);
    }

    /**
     * The next {@link MemoryBaseUnit} tha comes after this one (following the
     * English C system, like for the KILO the next would be MEGA, and for MEGA
     * the next would be GIGA etc.)
     */
    @Nullable
    public abstract MemoryBaseUnit next();
}
