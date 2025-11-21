package com.nucleonforge.axile.master.api.request;

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
    SCHEDULED_TASKS
}
