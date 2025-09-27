package com.nucleonforge.axile.common.domain;

import org.jspecify.annotations.NonNull;

/**
 * @param id            The id of the instance. This id must be unique among all the other instances that are
 *                      managed by this Axile Master.
 * @param actuatorUrl   The URL of the actuator root, e.g. {@code https://my-app:6061/actuator}
 */
public record InstanceReference(InstanceId id, @NonNull String actuatorUrl) {}
