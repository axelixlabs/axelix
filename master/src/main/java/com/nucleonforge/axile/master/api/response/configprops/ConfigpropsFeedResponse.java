package com.nucleonforge.axile.master.api.response.configprops;

import java.util.ArrayList;
import java.util.List;

/**
 * The feed of @ConfigurationProperties beans used in the application.
 *
 * @param beans  The unified list of beans that contains beans from one or more contexts.
 *
 * @author Sergey Cherkasov
 */
public record ConfigpropsFeedResponse(List<ConfigpropsProfile> beans) {

    public ConfigpropsFeedResponse() {
        this(new ArrayList<>());
    }

    public ConfigpropsFeedResponse addBean(ConfigpropsProfile beanProfile) {
        this.beans.add(beanProfile);
        return this;
    }
}
