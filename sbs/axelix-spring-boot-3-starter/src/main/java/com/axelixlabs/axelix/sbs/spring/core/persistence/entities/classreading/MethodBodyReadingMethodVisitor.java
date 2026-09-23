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
 * ASM method visitor that collects fields read and methods called by one method.
 *
 * @author Dmitry Mazurov
 */
final class MethodBodyReadingMethodVisitor extends MethodVisitor {

    private final MethodRef ref;
    private final int access;
    private final List<String> hierarchy;
    private final Map<MethodRef, MethodInfo> methods;

    private final MethodBody body = new MethodBody();

    MethodBodyReadingMethodVisitor(
            MethodRef ref, int access, List<String> hierarchy, Map<MethodRef, MethodInfo> methods) {

        super(SpringAsmInfo.ASM_VERSION);
        this.ref = ref;
        this.access = access;
        this.hierarchy = hierarchy;
        this.methods = methods;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String fieldName, String fieldDescriptor) {
        if (opcode == Opcodes.GETFIELD && hierarchy.contains(owner)) {
            body.addReadField(new FieldRef(owner, fieldName, fieldDescriptor));
        }
    }

    @Override
    public void visitMethodInsn(
            int opcode, String owner, String methodName, String methodDescriptor, boolean isInterface) {
        if (!hierarchy.contains(owner)) {
            return;
        }

        MethodRef call = MethodRef.of(owner, methodName, methodDescriptor);

        if (opcode == Opcodes.INVOKESPECIAL || opcode == Opcodes.INVOKESTATIC) {
            body.addExactCall(call);
        } else if (opcode == Opcodes.INVOKEVIRTUAL || opcode == Opcodes.INVOKEINTERFACE) {
            body.addVirtualCall(call);
        }
    }

    @Override
    public void visitEnd() {
        methods.put(ref, new MethodInfo(ref, access, body));
    }
}
