package com.nucleonforge.axile.master.api.request.state;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * {@link StateComponentSettings} for conditions.
 *
 * @author Mikhail Polivakha
 */
public class ConditionsStateComponentSettings implements StateComponentSettings {

    @JsonGetter(COMPONENT)
    @Override
    public StateExportComponent getComponent() {
        return StateExportComponent.CONDITIONS;
    }
}
