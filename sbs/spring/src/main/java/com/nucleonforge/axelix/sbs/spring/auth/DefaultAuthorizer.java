/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.spring.auth;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.nucleonforge.axelix.common.auth.core.Authority;
import com.nucleonforge.axelix.common.auth.core.AuthorizationRequest;
import com.nucleonforge.axelix.common.auth.core.DecodedUser;
import com.nucleonforge.axelix.common.auth.core.Role;

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
