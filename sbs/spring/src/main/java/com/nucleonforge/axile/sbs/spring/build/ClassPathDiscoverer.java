package com.nucleonforge.axile.sbs.spring.build;

import com.nucleonforge.axile.common.domain.ClassPath;

/**
 * The service that is capable to discover the {@link ClassPath} of the application.
 *
 * @author Mikhail Polivakha
 */
public interface ClassPathDiscoverer {

    /**
     * Discover the {@link ClassPath} of this application.
     */
    ClassPath discover();
}
