package com.nucleonforge.axile.master.api.response;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

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
            String beanName, String prefix, List<PropertyProfile> properties, List<KeyValue> inputs) {}

    /**
     * @param name              The name of the property.
     * @param value             The value of the property.
     * @param validationMessage The validation warning if value is invalid, if present - value is invalid.
     */
    public record PropertyProfile(String name, @Nullable String value, @Nullable String validationMessage) {}
}
