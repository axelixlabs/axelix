package com.nucleonforge.axile.master.service.export.settings;

import com.nucleonforge.axile.master.service.export.StateComponent;
import com.nucleonforge.axile.master.service.export.StateComponentSettings;

/**
 * {@link StateComponentSettings} for scheduled tasks.
 *
 * @author Mikhail Polivakha
 */
public class ScheduledTasksStateComponentSettings implements StateComponentSettings {

    @Override
    public StateComponent component() {
        return StateComponent.SCHEDULED_TASKS;
    }
}
