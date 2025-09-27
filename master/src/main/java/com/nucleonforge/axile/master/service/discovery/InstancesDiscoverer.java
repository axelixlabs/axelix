package com.nucleonforge.axile.master.service.discovery;

import java.util.Set;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.common.domain.InstanceReference;

/**
 * The SPI interface for discovering {@link InstanceReference instances} of running applications.
 *
 * <p>
 * There are, essentially, two ways to configure the deployment of the master and starters:
 * either master itself needs to discover instances, or the instances register themselves in
 * the master. This SPI interface exists specifically to implement the first approach.
 * <p>
 * Implementations may rely on certain environment to be present, such as K8S or consul, or
 * Netflix Eureka to for instance.
 *
 * @author Mikhail Polivakha
 */
public interface InstancesDiscoverer {

    /**
     * Perform actual discovery.
     */
    @NonNull
    Set<@NonNull InstanceReference> discover();

    /**
     * Return the discovered {@link Set} of {@link InstanceReference instance references}.
     * Safe variation of {@link #discover()}.
     */
    @NonNull
    default Set<@NonNull InstanceReference> discoverSafely() {
        try {
            return discover();
        } catch (Throwable t) {
            t.printStackTrace();
            return Set.of();
        }
    }
}
