package com.nucleonforge.axile.spring.build;

import java.util.Optional;

/**
 * The {@link InstanceIdDiscoverer} that is capable to extract the isntanceId
 * from the K8S environment.
 *
 * @author Mikhail Polivakha
 */
public class K8sInstanceIdDiscoverer implements InstanceIdDiscoverer {

    @Override
    public Optional<String> discover() {
        // TODO: implement
        throw new UnsupportedOperationException();
    }
}
