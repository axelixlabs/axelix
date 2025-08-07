package com.nucleonforge.axile.common.auth.core;

import java.util.Collections;
import java.util.Set;

/**
 * Default {@link Role} backed by real {@link #authorities}.
 *
 * @see Role
 * @since 16.07.25
 * @author Mikhail Polivakha
 */
public record DefaultRole(String name, Set<Authority> authorities, Set<Role> components) implements Role {

    public DefaultRole {
        if (authorities == null) {
            authorities = Collections.emptySet();
        }
        if (components == null) {
            components = Collections.emptySet();
        }
    }
}
