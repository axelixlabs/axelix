package com.nucleonforge.axile.master.api.response.configprops;

import java.util.ArrayList;
import java.util.List;

/**
 * @ConfigurationProperties beans used in the application that have given prefix.
 *
 * @param beans The unified list of beans that have the given prefix.
 *              (It is possible that we may have N @ConfigurationProperties beans with the same
 *              prefix, but in different application contexts. That is why we have the List here,
 *              and not a singe ConfigpropsProfile.)
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
