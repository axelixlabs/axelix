package com.nucleonforge.axile.master.service.export.settings;

import com.nucleonforge.axile.master.service.export.StateComponent;
import com.nucleonforge.axile.master.service.export.StateComponentSettings;

/**
 * {@link StateComponentSettings} for caches feed.
 *
 * @author Mikhail Polivakha
 */
public class CachesStateComponentSettings implements StateComponentSettings {

    @Override
    public StateComponent component() {
        return StateComponent.CACHES;
    }
}
