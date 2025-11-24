package com.nucleonforge.axile.master.service.export.settings;

import com.nucleonforge.axile.master.service.export.StateComponent;
import com.nucleonforge.axile.master.service.export.StateComponentSettings;

/**
 * {@link StateComponentSettings} for heap dumps.
 *
 * @param sanitized Whether to provide sanitized heap.
 * @author Mikhail Polivakha
 */
public record HeapDumpStateComponentSettings(boolean sanitized) implements StateComponentSettings {

    @Override
    public StateComponent component() {
        return StateComponent.HEAP_DUMP;
    }
}
