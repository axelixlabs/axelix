package com.nucleonforge.axile.master.service.export.settings;

import com.nucleonforge.axile.master.service.export.StateComponent;
import com.nucleonforge.axile.master.service.export.StateComponentSettings;

/**
 * {@link StateComponentSettings} for conditions.
 *
 * @author Mikhail Polivakha
 */
public class ConditionsStateComponentSettings implements StateComponentSettings {

    @Override
    public StateComponent component() {
        return StateComponent.CONDITIONS;
    }
}
