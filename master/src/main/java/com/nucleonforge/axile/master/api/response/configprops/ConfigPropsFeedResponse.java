package com.nucleonforge.axile.master.api.response.configprops;

import java.util.ArrayList;
import java.util.List;

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
}
