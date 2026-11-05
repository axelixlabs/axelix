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

import java.security.Principal;
import java.util.Objects;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.auth.core.SecurityContext;

/**
 * {@link Principal} that carries the Axelix {@link SecurityContext} of the request on the reactive stack.
 *
 * @since 20.10.2026
 * @author Nikita Kirillov
 */
public final class AxelixPrincipal implements Principal {

    private final SecurityContext securityContext;

    public AxelixPrincipal(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    public SecurityContext securityContext() {
        return securityContext;
    }

    @Override
    public String getName() {
        return securityContext.currentUser().getUsername();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AxelixPrincipal)) {
            return false;
        }
        return securityContext.equals(((AxelixPrincipal) other).securityContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(securityContext);
    }

    @Override
    public String toString() {
        return "AxelixPrincipal[securityContext=" + securityContext + "]";
    }
}
