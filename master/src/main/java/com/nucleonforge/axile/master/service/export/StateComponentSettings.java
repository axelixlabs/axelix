package com.nucleonforge.axile.master.service.export;

/**
 * Common interface for settings for a particular {@link StateComponent}.
 *
 * @author Mikhail Polivakha
 */
public interface StateComponentSettings {

    /**
     * @return the state components that these settings are menat for.
     */
    StateComponent component();
}
