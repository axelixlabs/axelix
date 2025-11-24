package com.nucleonforge.axile.master.api.request.state;

/**
 * {@link StateComponentSettings} for Thread Dump.
 *
 * @author Mikhail Polivakha
 */
public class ThreadDumpStateComponentSettings implements StateComponentSettings {

    @Override
    public StateExportComponent getComponent() {
        return StateExportComponent.THREAD_DUMP;
    }
}
