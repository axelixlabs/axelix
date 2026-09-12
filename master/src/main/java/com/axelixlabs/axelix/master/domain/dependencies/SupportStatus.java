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
package com.axelixlabs.axelix.master.domain.dependencies;

/**
 * What the authors of a project are still doing with it. This is the single question the curated catalog exists to
 * answer about every library a managed application resolves at runtime.
 * <p>
 * There is deliberately no {@code UNKNOWN} member. A library Axelix has nothing to say about is simply absent from
 * the catalog, which {@link com.axelixlabs.axelix.master.service.dependencies.LibraryCatalog} reports as an empty
 * lookup. Folding "not curated" into the status would make the two indistinguishable to a caller.
 *
 * @author Mikhail Polivakha
 */
public enum SupportStatus {

    /**
     * The project is actively developed: features still land and fixes are released on a normal cadence.
     */
    ACTIVE,

    /**
     * The project is in maintenance mode. Feature development has stopped and releases happen only for critical
     * fixes. Existing applications keep working, but building anything further on it is a decision to revisit.
     */
    MAINTENANCE,

    /**
     * The project is finished. No releases of any kind are expected, security fixes included.
     */
    SUNSET
}
