package com.nucleonforge.axile.master.service.versions;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.master.model.software.core.SpringBoot;

/**
 * The {@link SoftwareDistributionDiscoverer} for the {@link SpringBoot}.
 *
 * @author Mikhail Polivakha
 */
@Service
public class SpringBootDistributionDiscoverer extends LibrarySoftwareDistributionDiscoverer<SpringBoot> {

    protected SpringBootDistributionDiscoverer() {
        super(new SpringBoot());
    }
}
