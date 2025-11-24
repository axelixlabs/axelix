package com.nucleonforge.axile.master.service.export.settings;

import com.nucleonforge.axile.master.service.export.StateComponent;
import com.nucleonforge.axile.master.service.export.StateComponentSettings;

/**
 * {@link StateComponentSettings} for Thread Dump.
 *
 * @author Mikhail Polivakha
 */
public class ThreadDumpStateComponentSettings implements StateComponentSettings {

    @Override
    public StateComponent component() {
        return StateComponent.THREAD_DUMP;
    }
}
