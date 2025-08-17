package com.nucleonforge.axile.spring.build;

import com.nucleonforge.axile.common.domain.CommitInfo;

/**
 * Service that is capable to discover current {@link CommitInfo git commit info}.
 *
 * @author Mikhail Polivakha
 */
public interface CommitInformationDiscoverer {

    /**
     * Perform actual discovery
     */
    CommitInfo discover();
}
