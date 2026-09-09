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
package com.axelixlabs.axelix.master.service.auth.oauth;

import java.util.Map;

/**
 * Identity data obtained from a successfully validated OIDC ID token.
 *
 * @param username the username selected from the token claims (mutable, human-facing)
 * @param subject the {@code sub} claim - the provider-local, stable identifier of the user
 * @param claims all validated ID token claims
 *
 * @author Mikhail Polivakha
 */
public record ValidatedOidcIdentity(String username, String subject, Map<String, Object> claims) {

    public ValidatedOidcIdentity {
        claims = Map.copyOf(claims);
    }
}
