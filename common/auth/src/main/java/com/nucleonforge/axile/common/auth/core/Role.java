package com.nucleonforge.axile.common.auth.core;

import java.util.Set;

/**
 * SPI interface of a Role. A role is comprised from a set of {@link Authority authorities}.
 *
 * @see Authority
 * @since 16.07.25
 * @author Mikhail Polivakha
 */
public interface Role {

    /**
     * The unique name of this role.
     *
     * @return the name of the role.
     */
    String name();

    /**
     * Authorities of a given role.
     *
     * @return immutable set of {@link Authority} objects associated with this role
     */
    Set<Authority> authorities();

    /**
     * Component roles that are included in this role.
     * <p>
     * This allows defining hierarchical roles. The hierarchy must form a
     * <strong>directed acyclic graph (DAG)</strong>.
     * Implementations must ensure there are no duplicate or cyclic roles within the hierarchy.
     *
     * @return immutable set of {@link Role} objects included in this role
     */
    Set<Role> components();
}
