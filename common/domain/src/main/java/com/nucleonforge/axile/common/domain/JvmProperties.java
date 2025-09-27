package com.nucleonforge.axile.common.domain;

import java.util.Set;

/**
 * Contains JVM properties used during the launch of an application instance.
 *
 * @author Mikhail Polivakha
 */
public class JvmProperties {

    private Set<JvmProperty> jvmProperties;

    public JvmProperties(Set<JvmProperty> jvmProperties) {
        this.jvmProperties = jvmProperties;
    }
}
