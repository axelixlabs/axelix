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

import java.util.Optional;

import com.axelixlabs.axelix.common.auth.core.Authority;

/**
 * Interface for resolving a required {@link Authority}
 * based on the request path.
 *
 * @since 28.07.2025
 * @author Nikita Kirillov
 */
public interface AuthorityResolver {

    /**
     * Resolves the required {@link Authority} for the given request path.
     *
     * @param path the request path (e.g. "/actuator/axelix-beans")
     * @return an {@link Optional} containing the required {@link Authority},
     * or {@link Optional#empty()} if no authority is associated with the path
     */
    Optional<Authority> resolve(String path);
}
