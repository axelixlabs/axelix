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
package com.axelixlabs.axelix.sbs.spring.auth;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.AuthorizationRequest;
import com.axelixlabs.axelix.common.auth.core.DecodedUser;
import com.axelixlabs.axelix.common.auth.core.Role;

/**
 * Default implementation of {@link Authorizer}.
 *
 * @since 30.07.25
 * @author Nikita Kirillov
 */
public class DefaultAuthorizer implements Authorizer {

    @Override
    public void authorize(DecodedUser user, AuthorizationRequest authorizationRequest) throws AuthorizationException {
        Set<Authority> requiredAuthorities = authorizationRequest.requiredAuthorities();

        if (requiredAuthorities.isEmpty()) {
            return;
        }

        Set<String> userAuthorities = user.roles().stream()
                .flatMap(role -> collectAuthorities(role).stream())
                .map(Authority::getName)
                .collect(Collectors.toSet());

        Set<String> requiredNames =
                requiredAuthorities.stream().map(Authority::getName).collect(Collectors.toSet());

        if (!userAuthorities.containsAll(requiredNames)) {
            throw new AuthorizationException("Access denied: missing required authorities " + requiredNames);
        }
    }

    private Set<Authority> collectAuthorities(Role role) {
        Set<Authority> all = new HashSet<>(role.authorities());

        for (Role component : role.components()) {
            all.addAll(collectAuthorities(component));
        }

        return all;
    }
}
