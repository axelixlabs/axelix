CREATE TABLE IF NOT EXISTS instances (
    instance_id              TEXT    PRIMARY KEY,
    name                     TEXT    NOT NULL,
    service_version          TEXT    NOT NULL,
    java_version             TEXT    NOT NULL,
    spring_boot_version      TEXT    NOT NULL,
    spring_framework_version TEXT    NOT NULL,
    kotlin_version           TEXT,
    jdk_vendor               TEXT    NOT NULL,
    commit_sha_short         TEXT    NOT NULL,
    deployed_at              TEXT,
    status                   TEXT    NOT NULL,
    heap                     REAL    NOT NULL,
    actuator_url             TEXT    NOT NULL,
    vm_features              TEXT    NOT NULL DEFAULT '[]'
);