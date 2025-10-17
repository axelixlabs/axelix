package com.nucleonforge.axile.sbs.spring.master;

import java.util.Optional;

import com.nucleonforge.axile.common.api.registration.ShortBuildInfo;

/**
 * Component that is capable to discover the short build information for initial service discovery purposes.
 *
 * @author Mikhail Polivakha
 */
public interface ShortBuildInfoProvider {

    /**
     * @return the discovered {@link ShortBuildInfo}.
     */
    Optional<ShortBuildInfo> getShortBuildInfo();
}
