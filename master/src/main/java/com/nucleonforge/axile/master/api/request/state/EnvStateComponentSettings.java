package com.nucleonforge.axile.master.api.request.state;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * {@link StateComponentSettings} for the environment.
 *
 * @author Mikhail Polivakha
 */
public class EnvStateComponentSettings implements StateComponentSettings {

    @JsonGetter(COMPONENT)
    @Override
    public StateExportComponent getComponent() {
        return StateExportComponent.ENV;
    }
}
