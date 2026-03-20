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
package com.axelixlabs.axelix.sbs.spring.core.master;

import org.jspecify.annotations.NonNull;

/**
 * The function that is capable to serialize any given object to its JSON representation.
 *
 * @author Mikhail Polivakha
 */
public interface JsonSerializationFunction {

    /**
     * @param o object to be serialized.
     * @return serialized version of object.
     */
    @NonNull
    default String serialize(Object o) {
        try {
            return serializeInternal(o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    String serializeInternal(Object o) throws Exception;
}
