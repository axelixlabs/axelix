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
package com.axelixlabs.axelix.sbs.spring.core.persistence;

import java.lang.reflect.Modifier;

import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.MethodFilter;

/**
 * Checks whether a class is safe to proxy with CGLIB.
 *
 * @author Nikita Kirillov
 */
final class ProxyingUtils {

    private ProxyingUtils() {}

    static boolean isSafeToCGLIBProxy(Class<?> targetClass) {
        return !Modifier.isFinal(targetClass.getModifiers()) && !hasFinalProxyableMethod(targetClass);
    }

    private static boolean hasFinalProxyableMethod(Class<?> targetClass) {
        MethodFilter finalMethodFilter = method -> !ReflectionUtils.isObjectMethod(method)
                && !Modifier.isPrivate(method.getModifiers())
                && !Modifier.isStatic(method.getModifiers())
                && Modifier.isFinal(method.getModifiers());

        return ReflectionUtils.getUniqueDeclaredMethods(targetClass, finalMethodFilter).length > 0;
    }
}
