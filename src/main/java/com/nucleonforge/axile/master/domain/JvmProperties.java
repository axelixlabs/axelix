package com.nucleonforge.axile.master.domain;

import java.util.Set;

/**
 * Holder for the given {@link JvmProperty properties} being used during any given
 * {@link Instance} launch.
 *
 * @author Mikhail Polivakha
 */
public class JvmProperties {

    private Set<JvmProperty> jvmProperties;

    public JvmProperties(Set<JvmProperty> jvmProperties) {
        this.jvmProperties = jvmProperties;
    }
}
