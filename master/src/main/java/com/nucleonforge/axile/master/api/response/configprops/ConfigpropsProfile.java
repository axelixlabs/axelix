package com.nucleonforge.axile.master.api.response.configprops;

import java.util.Collections;
import java.util.Map;

/**
 * The profile of a given @ConfigurationProperties bean.
 *
 * @param beanName     The name of the bean.
 * @param prefix       The prefix applied to the names of the bean properties.
 * @param properties   The properties of the bean as name-value pairs.
 * @param inputs       The origin of each configuration parameter — which value was applied and from which source — to configure a specific property.
 *
 * @author Sergey Cherkasov
 */
public record ConfigpropsProfile(
        String beanName, String prefix, Map<String, Object> properties, Map<String, Object> inputs) {

    public ConfigpropsProfile {
        if (properties == null) {
            properties = Collections.emptyMap();
        }
        if (inputs == null) {
            inputs = Collections.emptyMap();
        }
    }
}
