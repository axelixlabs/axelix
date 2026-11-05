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
package com.axelixlabs.axelix.sbs.spring.core.persistence.entities.classreading;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * A method identified by its owner and JVM name+descriptor.
 *
 * @author Nikita Kirilloff
 */
public final class MethodRef {

    private final String owner;
    private final String key;

    /**
     * Creates a reference to a method.
     *
     * @param owner the declaring class' internal name
     * @param key the method name and JVM descriptor, e.g. {@code toString()Ljava/lang/String;}
     */
    public MethodRef(String owner, String key) {
        this.owner = owner;
        this.key = key;
    }

    public static MethodRef from(Method method) {
        StringBuilder descriptor = new StringBuilder("(");
        for (Class<?> parameterType : method.getParameterTypes()) {
            descriptor.append(JvmType.getDescriptor(parameterType));
        }
        descriptor.append(')').append(JvmType.getDescriptor(method.getReturnType()));
        return new MethodRef(JvmType.getInternalName(method.getDeclaringClass()), method.getName() + descriptor);
    }

    public static MethodRef of(String owner, String name, String descriptor) {
        return new MethodRef(owner, name + descriptor);
    }

    public String owner() {
        return owner;
    }

    public String key() {
        return key;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MethodRef)) {
            return false;
        }
        MethodRef methodRef = (MethodRef) other;
        return owner.equals(methodRef.owner) && key.equals(methodRef.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, key);
    }
}
