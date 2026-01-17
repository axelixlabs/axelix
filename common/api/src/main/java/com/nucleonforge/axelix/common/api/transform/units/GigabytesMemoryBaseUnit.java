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
 * Gigabytes {@link MemoryBaseUnit}.
 *
 * @since 18.12.2025
 * @author Nikita Kirillov
 */
public class GigabytesMemoryBaseUnit extends MemoryBaseUnit {

    public static final GigabytesMemoryBaseUnit INSTANCE = new GigabytesMemoryBaseUnit(Set.of("gigabytes"), "GB");

    public GigabytesMemoryBaseUnit(Set<String> aliases, String displayName) {
        super(aliases, displayName);
    }

    @Override
    @Nullable
    public MemoryBaseUnit next() {
        return null;
    }
}
