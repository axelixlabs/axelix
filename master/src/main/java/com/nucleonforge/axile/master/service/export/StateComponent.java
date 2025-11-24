package com.nucleonforge.axile.master.service.export;

public enum StateComponent {
    HEAP_DUMP("hprof"),
    THREAD_DUMP("json"),
    BEANS("json"),
    CACHES("json"),
    CONDITIONS("json"),
    CONFIG_PROPS("json"),
    ENV("json"),
    LOG_FILE("log"),
    SCHEDULED_TASKS("json");

    private final String fileExtension;

    StateComponent(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getFilename() {
        return name().toLowerCase() + "." + fileExtension;
    }
}
