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

import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.auth.core.SecurityContext;
import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.exception.AuthorizationException;

/**
 * Service that checks whether the current user in the SecurityContext has a specific authority.
 *
 * @author Nikita Kirillov
 */
public class RequiredAuthorityCheckService {

    private final SecurityContextExecutor securityContextExecutor;

    public RequiredAuthorityCheckService(SecurityContextExecutor securityContextExecutor) {
        this.securityContextExecutor = securityContextExecutor;
    }

    public boolean hasAuthority(DefaultAuthority requiredAuthority) throws AuthorizationException {
        return securityContextExecutor
                .getSecurityContext()
                .map(SecurityContext::currentUser)
                .map(user -> userHasAuthority(user, requiredAuthority))
                .orElseThrow(() -> new AuthorizationException("Missing required SecurityContext"));
    }

    private boolean userHasAuthority(User user, DefaultAuthority requiredAuthority) {
        return user.getRoles().stream()
                .flatMap(role -> role.getAuthorities().stream())
                .anyMatch(authority -> authority == requiredAuthority);
    }
}
