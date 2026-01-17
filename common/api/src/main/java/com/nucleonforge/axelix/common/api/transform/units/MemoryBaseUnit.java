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
package com.nucleonforge.axelix.common.api.transform.units;

import java.util.Set;

import org.jspecify.annotations.Nullable;

/**
 * {@link AbstractBaseUnit} of Memory. The memory is structured.
 *
 * @author Mikhail Polivakha
 */
public abstract class MemoryBaseUnit extends AbstractBaseUnit {

    public MemoryBaseUnit(Set<String> aliases, String displayName) {
        super(aliases, displayName);
    }

    /**
     * The next {@link MemoryBaseUnit} tha comes after this one (following the
     * English C system, like for the KILO the next would be MEGA, and for MEGA
     * the next would be GIGA etc.)
     */
    @Nullable
    public abstract MemoryBaseUnit next();
}
