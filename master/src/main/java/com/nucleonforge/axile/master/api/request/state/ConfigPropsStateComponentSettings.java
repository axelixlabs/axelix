package com.nucleonforge.axile.master.api.request.state;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * {@link StateComponentSettings} for config props.
 *
 * @author Mikhail Polivakha
 */
public class ConfigPropsStateComponentSettings implements StateComponentSettings {

    @JsonGetter(COMPONENT)
    @Override
    public StateExportComponent getComponent() {
        return StateExportComponent.CONFIG_PROPS;
    }
}
