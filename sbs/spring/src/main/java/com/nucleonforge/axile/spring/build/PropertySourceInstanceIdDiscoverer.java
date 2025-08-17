package com.nucleonforge.axile.spring.build;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * The {@link InstanceIdDiscoverer} that is based on Environment.
 *
 * @author Mikhail Polivakha
 */
@Service
public class PropertySourceInstanceIdDiscoverer implements InstanceIdDiscoverer {

    @Autowired
    private Environment environment;

    @Override
    public Optional<String> discover() {
        String instanceId;

        if ((instanceId = environment.getProperty("axile.sbs.registration.instance-id")) != null) {
            return Optional.of(instanceId);
        }

        if ((instanceId = environment.getProperty("spring.applicaiton.name")) != null) {
            return Optional.of(instanceId);
        }
        return Optional.empty();
    }
}
