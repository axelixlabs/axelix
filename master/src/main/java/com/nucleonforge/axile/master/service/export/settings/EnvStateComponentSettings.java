package com.nucleonforge.axile.master.service.export.settings;

import com.nucleonforge.axile.master.service.export.StateComponent;
import com.nucleonforge.axile.master.service.export.StateComponentSettings;

/**
 * {@link StateComponentSettings} for the environment.
 *
 * @author Mikhail Polivakha
 */
public class EnvStateComponentSettings implements StateComponentSettings {

    @Override
    public StateComponent component() {
        return StateComponent.ENV;
    }
}
