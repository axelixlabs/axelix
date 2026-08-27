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
package com.axelixlabs.axelix.common.auth.service;

import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.InternalAuthorities;

/**
 * An abstraction that serves as the single accessor for all the {@link Authority authorities}
 * in the application.
 * <p>
 * Note, that this abstraction is now aware of the {@link InternalAuthorities}.
 *
 * @author Mikhail Polivakha
 */
public interface AuthoritiesManager {

    /**
     * Constructs the necessary authority
     *
     * @return the authority associated with the given {@code name}, or {@code null}, if there is no.
     */
    @Nullable
    Authority decode(String name);

    /**
     * @return all the authorities this accessor is aware about.
     */
    Set<Authority> getAll();
}
