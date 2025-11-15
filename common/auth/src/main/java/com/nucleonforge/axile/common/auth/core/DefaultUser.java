package com.nucleonforge.axile.common.auth.core;

import java.util.Collections;
import java.util.Set;

/**
 * Default implementation of the {@link User} interface.
 * Represents a user with a username and a set of roles.
 *
 * @since 16.07.2025
 * @author Nikita Kirillov
 */
public record DefaultUser(String username, String password, Set<Role> roles) implements User {

    public DefaultUser {
        if (roles == null) {
            roles = Collections.emptySet();
        }
    }
}
