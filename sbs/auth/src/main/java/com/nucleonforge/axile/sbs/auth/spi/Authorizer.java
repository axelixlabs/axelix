package com.nucleonforge.axile.sbs.auth.spi;

import com.nucleonforge.axile.common.auth.core.AuthorizationRequest;
import com.nucleonforge.axile.common.auth.core.User;
import com.nucleonforge.axile.sbs.auth.AuthorizationException;
import com.nucleonforge.axile.sbs.auth.model.DecodedUser;

/**
 * SPI interface that is capable of authorizing the given {@link User} against an {@link AuthorizationRequest}.
 *
 * @since 16.07.25
 * @author Mikhail Polivakha
 */
public interface Authorizer {

    /**
     * Authorizes the given {@link User} against the specified {@link AuthorizationRequest}.
     *
     * @param user the user to authorize
     * @param authorizationRequest the request containing required authorities
     * @throws AuthorizationException if access is denied
     */
    void authorize(DecodedUser user, AuthorizationRequest authorizationRequest) throws AuthorizationException;
}
