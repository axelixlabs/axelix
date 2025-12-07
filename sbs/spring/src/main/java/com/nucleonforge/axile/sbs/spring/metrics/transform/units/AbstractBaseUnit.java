package com.nucleonforge.axile.sbs.spring.metrics.transform.units;

import java.util.Set;

/**
 * Abstract {@link BaseUnit}.
 *
 * @author Mikhail Polivakha
 */
public abstract class AbstractBaseUnit implements BaseUnit {

    /**
     * An array of aliases for the given base unit.
     * All members expected to be in lower case.
     */
    private final Set<String> aliases;

    private final String displayName;

    public AbstractBaseUnit(Set<String> aliases, String displayName) {
        this.aliases = aliases;
        this.displayName = displayName;
    }

    @Override
    public Set<String> getAliases() {
        return aliases;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }
}
