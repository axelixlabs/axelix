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

import java.util.List;
import java.util.Map;

import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.SpringAsmInfo;

/**
 * ASM method visitor that collects the fields read and the methods called by one method, considering
 * only field/method owners within the given class hierarchy. The resulting {@link MethodBody} is
 * published into {@code bodies} only once fully built, in {@link #visitEnd()}.
 *
 * @author Dmitry Mazurov
 */
final class MethodBodyReadingMethodVisitor extends MethodVisitor {

    private final MethodRef ref;
    private final List<String> hierarchy;
    private final Map<MethodRef, MethodBody> bodies;
    private final MethodBody body = new MethodBody();

    MethodBodyReadingMethodVisitor(MethodRef ref, List<String> hierarchy, Map<MethodRef, MethodBody> bodies) {
        super(SpringAsmInfo.ASM_VERSION);
        this.ref = ref;
        this.hierarchy = hierarchy;
        this.bodies = bodies;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String fieldName, String fieldDescriptor) {
        if (opcode == Opcodes.GETFIELD && hierarchy.contains(owner)) {
            body.addReadField(fieldName);
        }
    }

    @Override
    public void visitMethodInsn(
            int opcode, String owner, String methodName, String methodDescriptor, boolean isInterface) {
        if (hierarchy.contains(owner)) {
            body.addCall(new MethodRef(owner, methodName + methodDescriptor));
        }
    }

    @Override
    public void visitEnd() {
        bodies.put(ref, body);
    }
}
