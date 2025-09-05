package com.nucleonforge.axile.common.domain;

import java.util.Set;

/**
 * Holder for the given {@link JvmProperty properties} being used during any given
 * {@link Instance} launch.
 *
 * @author Mikhail Polivakha
 */
public class JvmProperties {

    private final Set<JvmProperty> jvmProperties;

    public JvmProperties(Set<JvmProperty> jvmProperties) {
        this.jvmProperties = jvmProperties;
    }

    public Set<JvmProperty> getJvmProperties() {
        return jvmProperties;
    }
}
