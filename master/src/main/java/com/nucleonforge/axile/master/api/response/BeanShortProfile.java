package com.nucleonforge.axile.master.api.response;

import java.util.Collections;
import java.util.Set;

/**
 * Short profile of a given bean.
 *
 * @param beanName     The name of the bean.
 * @param scope        The scope of the bean.
 * @param className    The fully qualified class name of the bean.
 * @param aliases      The aliases of the given bean.
 * @param dependencies The list of dependencies of this bean (i.e. other beans that this bean depends on).
 *
 * @author Mikhail Polivakha
 */
public record BeanShortProfile(
        String beanName, String scope, String className, Set<String> aliases, Set<String> dependencies) {

    public BeanShortProfile {
        if (aliases == null) {
            aliases = Collections.emptySet();
        }
        if (dependencies == null) {
            dependencies = Collections.emptySet();
        }
    }
}
