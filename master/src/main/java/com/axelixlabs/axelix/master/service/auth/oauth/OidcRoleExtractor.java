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

import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.master.exception.auth.OidcTokenExchangeException;

/**
 * The component that is capable to extract the role that belongs to current user based on the pair of {@link Tokens}.
 *
 * @author Mikhail Polivakha
 */
public interface OidcRoleExtractor {

    /**
     * Determine the {@link Role} of the user identified by the {@link Tokens provided tokens pair}.
     *
     * @param accessToken the access token granted to the user. Using this access token, the implementation is
     *                    supposed to determine the {@link Role} of the given user.
     *
     * @return the role that is extracted. Never {@code null}.
     * @throws OidcTokenExchangeException if the role cannot be extracted from neither
     *                                    the ID token nor the UserInfo endpoint
     */
    Role extractRole(String accessToken) throws OidcTokenExchangeException;

}
