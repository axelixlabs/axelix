package com.nucleonforge.axile.master.service.export.settings;

import com.nucleonforge.axile.master.service.export.StateComponent;
import com.nucleonforge.axile.master.service.export.StateComponentSettings;

/**
 * {@link StateComponentSettings} for log file
 *
 * @author Mikhail Polivakha
 */
public class LogFileStateComponentSettings implements StateComponentSettings {

    @Override
    public StateComponent component() {
        return StateComponent.LOG_FILE;
    }
}
