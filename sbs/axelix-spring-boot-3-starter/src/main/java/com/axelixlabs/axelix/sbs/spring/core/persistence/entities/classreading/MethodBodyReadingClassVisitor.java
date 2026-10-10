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
import java.util.Map;
import java.util.Set;

import org.springframework.asm.ClassVisitor;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.SpringAsmInfo;

/**
 * ASM class visitor that collects the methods of a class, delegating to
 * {@link MethodBodyReadingMethodVisitor} for each one.
 *
 * @author Dmitry Mazurov
 */
final class MethodBodyReadingClassVisitor extends ClassVisitor {

    private final String owner;
    private final Set<String> hierarchy;
    private final Map<MethodRef, MethodInfo> methods = new HashMap<>();

    MethodBodyReadingClassVisitor(String owner, Set<String> hierarchy) {
        super(SpringAsmInfo.ASM_VERSION);
        this.owner = owner;
        this.hierarchy = hierarchy;
    }

    @Override
    public MethodVisitor visitMethod(
            int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodRef ref = MethodRef.of(owner, name, descriptor);
        return new MethodBodyReadingMethodVisitor(ref, access, hierarchy, methods);
    }

    Map<MethodRef, MethodInfo> getMethods() {
        return methods;
    }
}
