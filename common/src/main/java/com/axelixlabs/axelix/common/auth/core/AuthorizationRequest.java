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
package com.axelixlabs.axelix.common.auth.core;

import java.util.Objects;
import java.util.Set;

/**
 * Request for authorization.
 *
 * @see Authority
 * @since 16.07.25
 * @author Mikhail Polivakha
 */
public final class AuthorizationRequest {

    private final Set<Authority> requiredAuthorities;

    public AuthorizationRequest(Set<Authority> requiredAuthorities) {
        this.requiredAuthorities = requiredAuthorities;
    }

    public Set<Authority> requiredAuthorities() {
        return requiredAuthorities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthorizationRequest that = (AuthorizationRequest) o;
        return Objects.equals(requiredAuthorities, that.requiredAuthorities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requiredAuthorities);
    }

    @Override
    public String toString() {
        return "AuthorizationRequest[" + "requiredAuthorities=" + requiredAuthorities + ']';
    }
}
