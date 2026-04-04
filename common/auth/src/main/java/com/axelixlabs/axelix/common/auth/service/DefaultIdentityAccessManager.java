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
package com.axelixlabs.axelix.common.auth.service;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.AuthorizationRequest;
import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.exception.AuthorizationException;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.JwtProcessingException;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;

/**
 * Default {@link IdentityAccessManager}. Handles the entire IAM.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public class DefaultIdentityAccessManager implements IdentityAccessManager {

    private final JwtDecoderService jwtDecoderService;
    private final AuthorityResolver authorityResolver;
    private final Authorizer authorizer;

    public DefaultIdentityAccessManager(
            JwtDecoderService jwtDecoderService, AuthorityResolver authorityResolver, Authorizer authorizer) {
        this.jwtDecoderService = jwtDecoderService;
        this.authorityResolver = authorityResolver;
        this.authorizer = authorizer;
    }

    @Override
    public void verifyAccess(String requestPath, HttpMethod requestHttpMethod, @Nullable String token)
            throws AuthorizationException, JwtProcessingException {

        if (token == null || token.isEmpty()) {
            throw new InvalidJwtTokenException("Authorization token is missing");
        }

        PasswordlessUser user = jwtDecoderService.decodeTokenToUser(token);

        Optional<Authority> requiredAuthority = authorityResolver.resolve(requestPath, requestHttpMethod);

        AuthorizationRequest authorizationRequest =
                new AuthorizationRequest(requiredAuthority.map(Set::of).orElse(Collections.emptySet()));

        authorizer.authorize(user, authorizationRequest);
    }
}
