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

import java.lang.reflect.Modifier;

/**
 * A method discovered in bytecode together with its access flags and body.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirilloff
 */
public final class MethodInfo {

    private final MethodRef ref;
    private final int access;
    private final MethodBody body;

    /**
     * Creates information about a bytecode method.
     *
     * @param ref the method's owner, name and JVM descriptor
     * @param access the method's JVM access flags
     * @param body the method's recorded bytecode calls
     */
    public MethodInfo(MethodRef ref, int access, MethodBody body) {
        this.ref = ref;
        this.access = access;
        this.body = body;
    }

    public MethodRef ref() {
        return ref;
    }

    public int access() {
        return access;
    }

    public MethodBody body() {
        return body;
    }

    public boolean isPrivate() {
        return Modifier.isPrivate(access);
    }

    public boolean isStatic() {
        return Modifier.isStatic(access);
    }

    public boolean isFinal() {
        return Modifier.isFinal(access);
    }

    public boolean cannotBeOverridden() {
        return isPrivate() || isFinal() || isStatic();
    }

    public boolean isPackagePrivate() {
        return !Modifier.isPublic(access) && !Modifier.isProtected(access) && !Modifier.isPrivate(access);
    }
}
