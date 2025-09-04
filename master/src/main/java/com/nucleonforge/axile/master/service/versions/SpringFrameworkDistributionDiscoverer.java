package com.nucleonforge.axile.master.service.versions;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.master.model.software.core.SpringFramework;

/**
 * The {@link SoftwareDistributionDiscoverer} for the Spring Framework.
 *
 * @author Mikhail Polivakha
 */
@Service
public class SpringFrameworkDistributionDiscoverer extends LibrarySoftwareDistributionDiscoverer<SpringFramework> {

    protected SpringFrameworkDistributionDiscoverer() {
        super(new SpringFramework());
    }
}
