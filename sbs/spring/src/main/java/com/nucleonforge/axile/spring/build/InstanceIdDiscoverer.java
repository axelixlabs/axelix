package com.nucleonforge.axile.spring.build;

import java.util.Optional;

/**
 * Service that is capable to discover the ID of the given Axile SBS instance
 *
 * @author Mikhail Polivakha
 */
public interface InstanceIdDiscoverer {

    /**
     * @return the discovered id of the instance, wrapped with {@link Optional},
     *         or {@link Optional#empty()} if this {@link InstanceIdDiscoverer} cannot resolve the instanceId
     */
    Optional<String> discover();
}
