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
import java.util.Objects;

import org.springframework.asm.Type;

/**
 * A field identified by its owner, name and JVM descriptor.
 *
 * @author Nikita Kirillov
 */
final class FieldRef {

    private final String owner;
    private final String name;
    private final String descriptor;

    /**
     * @param owner the internal name of the class that declares the field
     * @param name the field's name
     * @param descriptor the field's JVM descriptor
     */
    FieldRef(String owner, String name, String descriptor) {
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
    }

    static FieldRef from(Field field) {
        return new FieldRef(
                Type.getInternalName(field.getDeclaringClass()), field.getName(), Type.getDescriptor(field.getType()));
    }

    String owner() {
        return owner;
    }

    String name() {
        return name;
    }

    String descriptor() {
        return descriptor;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FieldRef)) {
            return false;
        }
        FieldRef fieldRef = (FieldRef) other;
        return owner.equals(fieldRef.owner) && name.equals(fieldRef.name) && descriptor.equals(fieldRef.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, name, descriptor);
    }
}
