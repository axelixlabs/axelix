package com.nucleonforge.axile.sbs.auth.filter;

import java.util.Optional;

import com.nucleonforge.axile.common.auth.core.Authority;

/**
 * Interface for resolving a required {@link Authority}
 * based on the request path.
 *
 * @since 28.07.2025
 * @author Nikita Kirillov
 */
public interface AuthorityResolver {

    /**
     * Resolves the required {@link Authority} for the given request path.
     *
     * @param path the request path (e.g. "/actuator/beans")
     * @return an {@link Optional} containing the required {@link Authority},
     * or {@link Optional#empty()} if no authority is associated with the path
     */
    Optional<Authority> resolve(String path);
}
