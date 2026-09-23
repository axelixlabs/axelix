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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.asm.ClassVisitor;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.SpringAsmInfo;

/**
 * ASM class visitor that, for every method declared in one class, delegates the field/call collection to
 * a {@link MethodBodyReadingMethodVisitor}. The result is available through {@link #getBodies()} once
 * this visitor has been passed to {@link org.springframework.asm.ClassReader#accept}.
 *
 * @author Dmitry Mazurov
 */
final class MethodBodyReadingClassVisitor extends ClassVisitor {

    private final String owner;
    private final List<String> hierarchy;
    private final Map<MethodRef, MethodBody> bodies = new HashMap<>();
    private final Set<MethodRef> privateMethods = new HashSet<>();

    MethodBodyReadingClassVisitor(String owner, List<String> hierarchy) {
        super(SpringAsmInfo.ASM_VERSION);
        this.owner = owner;
        this.hierarchy = hierarchy;
    }

    @Override
    public MethodVisitor visitMethod(
            int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodRef ref = new MethodRef(owner, name + descriptor);
        if ((access & Opcodes.ACC_PRIVATE) != 0) {
            // javac compiles a private method's self-call as invokevirtual too, so opcode alone can't
            // tell exact from virtual here - the declaring class' own access flags must.
            privateMethods.add(ref);
        }
        return new MethodBodyReadingMethodVisitor(ref, hierarchy, bodies);
    }

    Map<MethodRef, MethodBody> getBodies() {
        return bodies;
    }

    Set<MethodRef> getPrivateMethods() {
        return privateMethods;
    }
}
