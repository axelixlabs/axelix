package com.nucleonforge.axile.master.service.discovery;

import java.util.Set;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.common.domain.Instance;

/**
 *
 *
 * @author Mikhail Polivakha
 */
public interface InstancesDiscoverer {

    /**
     * Discoverer
     * @return
     */
    @NonNull
    Set<@NonNull Instance> discover();
}
