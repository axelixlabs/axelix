package com.nucleonforge.axile.master.api.request.state;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * {@link StateComponentSettings} for scheduled tasks.
 *
 * @author Mikhail Polivakha
 */
public class ScheduledTasksStateComponentSettings implements StateComponentSettings {

    @JsonGetter(COMPONENT)
    @Override
    public StateExportComponent getComponent() {
        return StateExportComponent.SCHEDULED_TASKS;
    }
}
