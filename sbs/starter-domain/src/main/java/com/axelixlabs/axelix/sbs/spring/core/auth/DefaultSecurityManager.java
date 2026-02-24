/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.core.auth;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.auth.JwtDecoderService;
import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.AuthorizationRequest;
import com.axelixlabs.axelix.common.auth.core.DecodedUser;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.JwtProcessingException;

/**
 * Default {@link SecurityManager}. First it authenticates the request, and then authorizes it.
 *
 * @author Mikhail Polivakha
 */
public class DefaultSecurityManager implements SecurityManager {

    private final JwtDecoderService jwtDecoderService;
    private final AuthorityResolver authorityResolver;
    private final Authorizer authorizer;

    public DefaultSecurityManager(
            JwtDecoderService jwtDecoderService, AuthorityResolver authorityResolver, Authorizer authorizer) {
        this.jwtDecoderService = jwtDecoderService;
        this.authorityResolver = authorityResolver;
        this.authorizer = authorizer;
    }

    @Override
    public boolean shouldAuthorize(String requestPath) {
        return requestPath.startsWith("/actuator/axelix-");
    }

    @Override
    public void authorizeInternal(String requestPath, @Nullable String token)
            throws AuthorizationException, JwtProcessingException {

        if (token == null || token.isEmpty()) {
            throw new InvalidJwtTokenException("Authorization token is missing");
        }

        DecodedUser user = jwtDecoderService.decodeTokenToUser(token);

        Optional<Authority> requiredAuthority = authorityResolver.resolve(requestPath);

        AuthorizationRequest authorizationRequest =
                new AuthorizationRequest(requiredAuthority.map(Set::of).orElse(Collections.emptySet()));

        authorizer.authorize(user, authorizationRequest);
    }
}
