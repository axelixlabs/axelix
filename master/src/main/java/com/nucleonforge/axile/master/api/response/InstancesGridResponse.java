package com.nucleonforge.axile.master.api.response;

import java.util.Collection;

import org.jspecify.annotations.Nullable;

/**
 * The grid of {@link InstanceShortProfile instances}.
 *
 * @author Mikhail Polivakha
 */
public record InstancesGridResponse(Collection<InstanceShortProfile> profiles) {

    /**
     * The short profile of the particular Instance, managed by this master deployment.
     *
     * @param deployedFor a String representation for how long the service has been already deployed for.
     *
     * @author Mikhail Polivakha
     */
    public record InstanceShortProfile(
            String name,
            String serviceVersion,
            String commitShaShort,
            InstanceStatus status,
            @Nullable String deployedFor,
            String javaVersion,
            String springBootVersion) {}

    /**
     * The state of the given instance.
     *
     * @author Mikhail Polivakha
     */
    public enum InstanceStatus {
        UP,
        DOWN,
        UNKNOWN
    }
}
