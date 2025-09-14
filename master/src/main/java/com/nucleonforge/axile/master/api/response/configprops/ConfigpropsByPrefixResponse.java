package com.nucleonforge.axile.master.api.response.configprops;

import java.util.ArrayList;
import java.util.List;

/**
 * The feed of @ConfigurationProperties bean used in the application.
 *
 * @author Sergey Cherkasov
 */
public record ConfigpropsByPrefixResponse(List<ConfigpropsProfile> beans) {

    public ConfigpropsByPrefixResponse() {
        this(new ArrayList<>());
    }

    public ConfigpropsByPrefixResponse addBean(ConfigpropsProfile beanProfile) {
        this.beans.add(beanProfile);
        return this;
    }
}
