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
package com.axelixlabs.axelix.master.service.auth;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.AuthorizationRequest;
import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.exception.AuthorizationException;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.JwtProcessingException;
import com.axelixlabs.axelix.common.auth.service.Authorizer;
import com.axelixlabs.axelix.common.auth.service.JwtDecoderService;
import com.axelixlabs.axelix.common.auth.service.WebIdentityAccessManager;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;

/**
 * The master-side {@link WebIdentityAccessManager}. It handles the entire IAM for the browser-facing HTTP requests,
 * resolving the required {@link Authority} directly from the {@link MasterWebEndpoint} that the request addresses
 * (via {@link MasterWebEndpointResolver}) rather than from a generic authority resolver.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public class MasterWebIdentityAccessManager implements WebIdentityAccessManager {

    private final JwtDecoderService jwtDecoderService;
    private final MasterWebEndpointResolver endpointResolver;
    private final Authorizer authorizer;

    public MasterWebIdentityAccessManager(
            JwtDecoderService jwtDecoderService, MasterWebEndpointResolver endpointResolver, Authorizer authorizer) {
        this.jwtDecoderService = jwtDecoderService;
        this.endpointResolver = endpointResolver;
        this.authorizer = authorizer;
    }

    @Override
    public User verifyAccess(String relativeRequestPath, HttpMethod requestHttpMethod, @Nullable String token)
            throws AuthorizationException, JwtProcessingException {

        if (token == null || token.isEmpty()) {
            throw new InvalidJwtTokenException("Authorization token is missing");
        }

        Optional<Authority> requiredAuthority = endpointResolver
            .resolveEndpoint(relativeRequestPath, requestHttpMethod)
            .map(MasterWebEndpoint::authority);

        PasswordlessUser user = jwtDecoderService.decodeTokenToUser(token);

        AuthorizationRequest authorizationRequest =
                new AuthorizationRequest(requiredAuthority.map(Set::of).orElse(Collections.emptySet()));

        authorizer.authorize(user, authorizationRequest);

        return user;
    }
}
