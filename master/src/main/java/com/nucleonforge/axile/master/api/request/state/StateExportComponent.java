package com.nucleonforge.axile.master.api.request.state;

import org.jspecify.annotations.Nullable;

/**
 * The possible exportable state component.
 *
 * @see com.nucleonforge.axile.master.api.StateExportApi
 * @author Mikhail Polivakha
 */
public enum StateExportComponent {
    HEAP_DUMP,
    THREAD_DUMP,
    BEANS,
    CACHES,
    CONDITIONS,
    CONFIG_PROPS,
    ENV,
    LOG_FILE,
    SCHEDULED_TASKS;

    @Nullable
    public static StateExportComponent valueOfIgnoreCase(String component) {
        for (StateExportComponent value : values()) {
            if (value.name().equalsIgnoreCase(component)) {
                return value;
            }
        }

        return null;
    }
}
