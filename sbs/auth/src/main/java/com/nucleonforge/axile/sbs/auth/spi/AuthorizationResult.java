package com.nucleonforge.axile.sbs.auth.spi;

import com.nucleonforge.axile.common.auth.core.AuthorizationRequest;

/**
 * Result of the {@link Authorizer} analysis of the given {@link AuthorizationRequest}.
 *
 * @since 16.07.25
 * @author Mikhail Polivakha
 */
public interface AuthorizationResult {

    /**
     * @return whether the access is authorized
     */
    boolean accessAuthorized();
}
