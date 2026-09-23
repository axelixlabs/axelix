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

import java.util.HashSet;
import java.util.Set;

/**
 * The fields read and the methods called by a single method.
 *
 * <p>Calls are split by dispatch semantics: {@code exactCalls} are statically resolved calls such as
 * {@code invokespecial} and {@code invokestatic}, while {@code virtualCalls} require runtime method
 * selection.
 *
 * @author Dmitry Mazurov
 */
final class MethodBody {

    private final Set<FieldRef> readFields = new HashSet<>();
    private final Set<MethodRef> virtualCalls = new HashSet<>();
    private final Set<MethodRef> exactCalls = new HashSet<>();

    void addReadField(FieldRef field) {
        readFields.add(field);
    }

    void addVirtualCall(MethodRef call) {
        virtualCalls.add(call);
    }

    void addExactCall(MethodRef call) {
        exactCalls.add(call);
    }

    Set<FieldRef> getReadFields() {
        return readFields;
    }

    Set<MethodRef> getVirtualCalls() {
        return virtualCalls;
    }

    Set<MethodRef> getExactCalls() {
        return exactCalls;
    }
}
