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

/**
 * A field identified by its owner, name and JVM descriptor.
 *
 * @param owner the declaring class' internal name
 * @param name the field's name
 * @param descriptor the field's JVM type descriptor
 * @author Nikita Kirilloff
 */
public final class FieldRef {

    private final String owner;
    private final String name;
    private final String descriptor;

    public FieldRef(String owner, String name, String descriptor) {
        this.owner = owner;
        this.name = name;
        this.descriptor = descriptor;
    }

    public String owner() {
        return owner;
    }

    public String name() {
        return name;
    }

    public String descriptor() {
        return descriptor;
    }

    public static FieldRef from(Field field) {
        return new FieldRef(
                JvmType.getInternalName(field.getDeclaringClass()),
                field.getName(),
                JvmType.getDescriptor(field.getType()));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FieldRef)) {
            return false;
        }
        FieldRef that = (FieldRef) other;
        return owner.equals(that.owner) && name.equals(that.name) && descriptor.equals(that.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, name, descriptor);
    }
}
