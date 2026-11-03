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

import org.springframework.asm.Type;

/**
 * A method identified by its owner and ASM name+descriptor.
 *
 * @param owner the declaring class' internal name
 * @param key the ASM name+descriptor, e.g. {@code toString()Ljava/lang/String;}
 * @author Dmitry Mazurov
 */
record MethodRef(String owner, String key) {

    static MethodRef from(Method method) {
        return new MethodRef(
                Type.getInternalName(method.getDeclaringClass()), method.getName() + Type.getMethodDescriptor(method));
    }

    static MethodRef of(String owner, String name, String descriptor) {
        return new MethodRef(owner, name + descriptor);
    }
}
