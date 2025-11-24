package com.nucleonforge.axile.master.api.request.state;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * {@link StateComponentSettings} for heap dumps.
 *
 * @param sanitized Whether to provide sanitized heap.
 * @author Mikhail Polivakha
 */
public record HeapDumpStateComponentSettings(boolean sanitized) implements StateComponentSettings {

    @JsonGetter(COMPONENT)
    @Override
    public StateExportComponent getComponent() {
        return StateExportComponent.HEAP_DUMP;
    }
}
