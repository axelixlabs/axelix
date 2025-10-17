package com.nucleonforge.axile.master.model.instance;

import java.time.Instant;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * @param id                 The id of the instance. This id must be unique among all the other instances that are
 *                           managed by this Axile Master.
 * @param name               Displayable name of the instance
 * @param serviceVersion     Displayable version of the instance itself (not version of our starter inside Instance)
 * @param javaVersion        Version of the Java Platform used inside the service
 * @param springBootVersion  Version of the Spring Boot used inside the service
 * @param commitShaShort     Short git commit hash from which this instance's {@link #serviceVersion version} was build
 * @param deployedAt         Timestamp when the service was deployed
 * @param status             The status of the given instance from the Master standpoint.
 * @param actuatorUrl        The URL of the actuator root, e.g. {@code https://my-app:6061/actuator}
 */
public record Instance(
        InstanceId id,
        String name,
        String serviceVersion,
        String javaVersion,
        String springBootVersion,
        String commitShaShort,
        @Nullable Instant deployedAt,
        InstanceStatus status,
        @NonNull String actuatorUrl) {

    public enum InstanceStatus {
        UP,
        DOWN,
        UNKNOWN
    }
}
