package com.nucleonforge.axile.common.auth.core;

import java.util.Set;

/**
 * Request for authorization.
 *
 * @see Authority
 * @since 16.07.25
 * @author Mikhail Polivakha
 */
public record AuthorizationRequest(Set<Authority> requiredAuthorities) {}
