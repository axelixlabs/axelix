package com.nucleonforge.axile.spring.build;

import com.nucleonforge.axile.common.domain.JvmNonStandardOptions;

/**
 * Service that is capable to discover the {@link JvmNonStandardOptions non-standard VM options}
 * used by this instance.
 *
 * @author Mikhail Polivakha
 */
public interface NonStandardVMOptionsDiscoverer {

    /**
     * Perform actual discovery
     */
    JvmNonStandardOptions discover();
}
