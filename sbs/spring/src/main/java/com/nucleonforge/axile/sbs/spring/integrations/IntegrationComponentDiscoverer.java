package com.nucleonforge.axile.sbs.spring.integrations;

import java.util.Set;

/**
 * Implementations of this interface are capable to discover specific {@link Integration integrations}.
 *
 * @since 05.07.25
 * @author Mikhail Polivakha
 */
public interface IntegrationComponentDiscoverer<T extends Integration> {

    Set<T> discoverIntegrations();
}
