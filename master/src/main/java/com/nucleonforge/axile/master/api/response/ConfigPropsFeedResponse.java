package com.nucleonforge.axile.master.api.response;

import java.util.ArrayList;
import java.util.List;

import com.nucleonforge.axile.common.api.KeyValue;

/**
 * The feed of {@code @ConfigurationProperties} beans used in the application.
 *
 * @param beans  The unified list of beans that contains beans from one or more contexts.
 *
 * @author Sergey Cherkasov
 */
public record ConfigPropsFeedResponse(List<ConfigPropsProfile> beans) {

    public ConfigPropsFeedResponse() {
        this(new ArrayList<>());
    }

    public ConfigPropsFeedResponse addBean(ConfigPropsProfile beanProfile) {
        this.beans.add(beanProfile);
        return this;
    }

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
    public record ConfigPropsProfile(
            String beanName, String prefix, List<KeyValue> properties, List<KeyValue> inputs) {}
}
