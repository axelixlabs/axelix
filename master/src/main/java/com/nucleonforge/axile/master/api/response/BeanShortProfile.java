package com.nucleonforge.axile.master.api.response;

import java.util.Collections;
import java.util.Set;

/**
 * Short profile of a given beans.
 *
 * @param beanName     The name of the beans.
 * @param scope        The scope of the beans.
 * @param className    The fully qualified class name of the beans.
 * @param aliases      The aliases of the given beans.
 * @param dependencies The list of dependencies of this beans (i.e. other beans that this beans depends on).
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
