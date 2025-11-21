package com.nucleonforge.axile.master.api.response.configprops;

import java.util.Collections;
import java.util.List;

import com.nucleonforge.axile.common.api.KeyValue;

/**
 * The profile of a given {@code @ConfigurationProperties} bean.
 *
 * @param beanName     The name of the bean.
 * @param prefix       The prefix applied to the names of the bean properties.
 * @param properties   The properties of the bean as name-value pairs.
 * @param inputs       The origin and value of each configuration parameter
 *                     — which value was applied and from which source
 *                     — to configure a specific property.
 *
 * @author Sergey Cherkasov
 */
public record ConfigPropsProfile(String beanName, String prefix, List<KeyValue> properties, List<KeyValue> inputs) {

    public ConfigPropsProfile {
        if (properties == null) {
            properties = Collections.emptyList();
        }
        if (inputs == null) {
            inputs = Collections.emptyList();
        }
    }
}
