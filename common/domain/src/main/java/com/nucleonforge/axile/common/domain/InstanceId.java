package com.nucleonforge.axile.common.domain;

import org.jspecify.annotations.NonNull;

/**
 * The wrapper class around the value that represents the ID of the given {@link InstanceReference}.
 *
 * @author Mikhail Polivakha
 */
public record InstanceId(@NonNull String instanceId) {

    public static InstanceId of(@NonNull String instanceId) {
        return new InstanceId(instanceId);
    }
}
