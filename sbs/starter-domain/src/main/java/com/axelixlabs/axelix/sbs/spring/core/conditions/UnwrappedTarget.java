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
package com.axelixlabs.axelix.sbs.spring.core.conditions;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

/**
 * Represents the class and method names extracted from the target condition.
 *
 * @author Sergey Cherkasov
 */
public class UnwrappedTarget {

    private final String className;

    @Nullable
    private final String methodName;

    /**
     * Create new UnwrappedTarget.
     *
     * @param className  the class name of the class on which either the conditional annotation resided, or
     *                   that contained the {@link #methodName} on which the conditional annotation resided.
     *                   Guaranteed to be present.
     * @param methodName the name of the method on which the conditional annotation was put.
     */
    public UnwrappedTarget(String className, @Nullable String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    public String getClassName() {
        return className;
    }

    public @Nullable String getMethodName() {
        return methodName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UnwrappedTarget that = (UnwrappedTarget) o;
        return Objects.equals(className, that.className) && Objects.equals(methodName, that.methodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName);
    }

    @Override
    public String toString() {
        return "UnwrappedTarget{" + "className='" + className + '\'' + ", methodName='" + methodName + '\'' + '}';
    }
}
