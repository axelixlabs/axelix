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
package com.axelixlabs.axelix.common.utils;

import java.util.Set;

/**
 * Utils for working with Java Collection Framework.
 *
 * @author Mikhail Polivakha
 */
public class CollectionUtils {

    public static <T> Set<T> defaultIfEmpty(Set<T> source, T defaultValue) {
        if (source == null || source.isEmpty()) {
            return Set.of(defaultValue);
        } else {
            return source;
        }
    }
}
