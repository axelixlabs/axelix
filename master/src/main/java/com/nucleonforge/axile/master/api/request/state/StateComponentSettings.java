package com.nucleonforge.axile.master.api.request.state;

/**
 * The state export component.
 *
 * @author Mikhail Polivakha
 */
public interface StateComponentSettings {

    String COMPONENT = "component";

    /**
     * @return the state export component.
     */
    StateExportComponent getComponent();
}
