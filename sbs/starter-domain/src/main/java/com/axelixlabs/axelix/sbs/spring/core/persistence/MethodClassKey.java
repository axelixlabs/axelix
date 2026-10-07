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

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Key for caching method-specific transaction metadata per class the method is attributed to.
 * Similar to Spring's org.springframework.core.MethodClassKey.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @since 22.01.2026
 */
public final class MethodClassKey {
    private final Method method;
    private final Class<?> identityClass;

    /**
     * @param method        the method being analyzed
     * @param identityClass the class the method is attributed to
     */
    public MethodClassKey(Method method, Class<?> identityClass) {
        this.method = method;
        this.identityClass = identityClass;
    }

    public Method getMethod() {
        return method;
    }

    public Class<?> getIdentityClass() {
        return identityClass;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        var that = (MethodClassKey) obj;
        return Objects.equals(this.method, that.method) && Objects.equals(this.identityClass, that.identityClass);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, identityClass);
    }

    @Override
    public String toString() {
        return "MethodClassKey[" + "method=" + method + ", " + "identityClass=" + identityClass + ']';
    }
}
