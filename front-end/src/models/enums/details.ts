/**
 * The component of the Details Card that can be copied
 */
export enum ECopyableField {
    COMMIT_SHA_SHORT = "commitShaShort",
    BRANCH = "branch",
    ARTIFACT = "artifact",
}

/**
 * The particular component of the state that can be exported.
 */
export enum EExportableComponent {
    HEAP_DUMP = "HEAP_DUMP",
    THREAD_DUMP = "THREAD_DUMP",
    BEANS = "BEANS",
    CACHES = "CACHES",
    CONDITIONS = "CONDITIONS",
    CONFIG_PROPS = "CONFIG_PROPS",
    ENV = "ENV",
    LOG_FILE = "LOG_FILE",
    SCHEDULED_TASKS = "SCHEDULED_TASKS",
}
