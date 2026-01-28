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
package com.axelixlabs.axelix.sbs.spring.utils;

import java.lang.reflect.Field;

import org.springframework.util.ReflectionUtils;

/**
 * Utils for working with {@link Field} descriptors.
 *
 * @author Mikhail Polivakha
 */
public class FieldUtils {

    @SuppressWarnings({"unchecked", "NullAway"})
    public static <T> T getField(String name, Object onObject) {
        try {
            Field field = ReflectionUtils.findField(onObject.getClass(), name);
            if (field == null) {
                throw new NoSuchFieldException(name);
            }
            ReflectionUtils.makeAccessible(field);
            return (T) ReflectionUtils.getField(field, onObject);
        } catch (ClassCastException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
