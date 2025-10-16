package com.nucleonforge.axile.sbs.spring.integrations;

import java.util.HashMap;
import java.util.Map;

/**
 * The integration that service has with some other entity on the network
 *
 * @since 05.07.25
 * @author Mikhail Polivakha
 */
public sealed interface Integration permits AbstractIntegration {

    /**
     * @return abstract term that defines the type of entity with which the integration takes place
     */
    String entityType();

    /**
     * Protocol being used for communication
     */
    String protocol();

    /**
     * @return network address being used inside the app for communicating with this entity
     */
    String networkAddress();

    /**
     * @return key-value pairs, that represent some properties, that are specific to this integration or integration entity
     */
    default Map<String, Object> properties() {
        return new HashMap<>(0);
    }
}
