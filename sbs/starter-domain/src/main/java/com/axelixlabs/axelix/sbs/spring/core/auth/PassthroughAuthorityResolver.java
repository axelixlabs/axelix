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

import java.util.Optional;

import com.axelixlabs.axelix.common.auth.core.Authority;

/**
 * The passthrough {@link AuthorityResolver} - no authorities are required to access the given emdpoint.
 *
 * @deprecated That class should not be used or relied upon. It is a temporary solution until
 *             we came up with the reasonable authorization configuration for master --> starter
 *             communication
 * @author Mikhail Polivakha
 */
@Deprecated(forRemoval = true)
public class PassthroughAuthorityResolver implements AuthorityResolver {

    @Override
    public Optional<Authority> resolve(String path) {
        return Optional.empty();
    }
}
