package com.nucleonforge.axile.spring.build;

import com.nucleonforge.axile.common.domain.JvmProperties;

/**
 * Service that is capable to discover {@link JvmProperties JVM Properties} used by this instance.
 *
 * @author Mikhail Polivakha
 */
public interface PropertiesDiscoverer {

    /**
     * Perform actual discovery
     */
    JvmProperties discover();
}
