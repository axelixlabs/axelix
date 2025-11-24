package com.nucleonforge.axile.master.service.export.settings;

import com.nucleonforge.axile.master.service.export.StateComponent;
import com.nucleonforge.axile.master.service.export.StateComponentSettings;

/**
 * {@link StateComponentSettings} for config props.
 *
 * @author Mikhail Polivakha
 */
public class ConfigPropsStateComponentSettings implements StateComponentSettings {

    @Override
    public StateComponent component() {
        return StateComponent.CONFIG_PROPS;
    }
}
