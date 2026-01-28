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
package com.axelixlabs.axelix.common.api.transform.units;

import java.util.Set;

/**
 * Megabytes {@link MemoryBaseUnit}.
 *
 * @author Mikhail Polivakha
 */
public class MegabytesMemoryBaseUnit extends MemoryBaseUnit {

    public static final MegabytesMemoryBaseUnit INSTANCE = new MegabytesMemoryBaseUnit(Set.of("megabytes"), "MB");

    public MegabytesMemoryBaseUnit(Set<String> aliases, String displayName) {
        super(aliases, displayName);
    }

    @Override
    public MemoryBaseUnit next() {
        return GigabytesMemoryBaseUnit.INSTANCE;
    }
}
