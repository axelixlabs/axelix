package com.nucleonforge.axile.common.auth.core;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Represents a simplified, serializable role model used within JWT tokens.
 * <p>
 * This DTO is used to store role information in the form of a role name and
 * a set of authority names, making it suitable for inclusion in JWT claims.
 * </p>
 *
 * @param name the name of the role (e.g., the class name of the original role object)
 * @param authorities the set of authority names assigned to the role
 * @param components the list of nested roles that are part of this role (i.e. its components)
 *
 * @since 22.07.2025
 * @author Nikita Kirillov
 */
public record JwtRole(String name, Set<String> authorities, List<JwtRole> components) {

    public JwtRole {
        if (authorities == null) {
            authorities = Collections.emptySet();
        }
        if (components == null) {
            components = Collections.emptyList();
        }
    }
}
