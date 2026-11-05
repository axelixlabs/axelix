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

import org.springframework.asm.Opcodes;

/**
 * A method discovered in bytecode together with its access flags and body.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
final class MethodInfo {

    private final MethodRef ref;
    private final int access;
    private final MethodBody body;

    /**
     * @param ref the method's owner, name and JVM descriptor
     * @param access the method's JVM access flags
     * @param body the method's recorded bytecode calls
     */
    MethodInfo(MethodRef ref, int access, MethodBody body) {
        this.ref = ref;
        this.access = access;
        this.body = body;
    }

    MethodRef ref() {
        return ref;
    }

    int access() {
        return access;
    }

    MethodBody body() {
        return body;
    }

    boolean isPrivate() {
        return (access & Opcodes.ACC_PRIVATE) != 0;
    }

    boolean isStatic() {
        return (access & Opcodes.ACC_STATIC) != 0;
    }

    boolean isFinal() {
        return (access & Opcodes.ACC_FINAL) != 0;
    }

    boolean cannotBeOverridden() {
        return isPrivate() || isFinal() || isStatic();
    }

    boolean isPackagePrivate() {
        return (access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED | Opcodes.ACC_PRIVATE)) == 0;
    }
}
