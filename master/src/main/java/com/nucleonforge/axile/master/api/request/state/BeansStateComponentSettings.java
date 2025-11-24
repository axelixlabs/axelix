package com.nucleonforge.axile.master.api.request.state;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * {@link StateComponentSettings} for beans feed.
 *
 * @author Mikhail Polivakha
 */
public class BeansStateComponentSettings implements StateComponentSettings {

    @JsonGetter(COMPONENT)
    @Override
    public StateExportComponent getComponent() {
        return StateExportComponent.BEANS;
    }
}
