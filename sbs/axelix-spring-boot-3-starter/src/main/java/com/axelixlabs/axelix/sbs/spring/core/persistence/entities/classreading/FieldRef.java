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

import java.lang.reflect.Field;

import org.springframework.asm.Type;

/**
 * A field identified by its owner, name and JVM descriptor.
 *
 * @param owner the declaring class' internal name
 * @param name the field's name
 * @param descriptor the field's JVM type descriptor
 * @author Dmitry Mazurov
 */
record FieldRef(String owner, String name, String descriptor) {

    static FieldRef from(Field field) {
        return new FieldRef(
                Type.getInternalName(field.getDeclaringClass()), field.getName(), Type.getDescriptor(field.getType()));
    }
}
