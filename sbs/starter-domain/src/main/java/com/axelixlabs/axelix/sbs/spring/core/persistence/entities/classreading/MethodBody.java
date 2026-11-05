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
 * The fields read and methods called by a single method.
 *
 * @author Nikita Kirilloff
 */
public final class MethodBody {

    /** Fields read via {@code GETFIELD} within the entity hierarchy. */
    private final Set<FieldRef> readFields = new HashSet<>();

    /** Calls requiring runtime dispatch, i.e. {@code invokevirtual}/{@code invokeinterface}. */
    private final Set<MethodRef> virtualCalls = new HashSet<>();

    /** Statically resolved calls, i.e. {@code invokespecial}/{@code invokestatic}. */
    private final Set<MethodRef> exactCalls = new HashSet<>();

    public void addReadField(FieldRef field) {
        readFields.add(field);
    }

    public void addVirtualCall(MethodRef call) {
        virtualCalls.add(call);
    }

    public void addExactCall(MethodRef call) {
        exactCalls.add(call);
    }

    public Set<FieldRef> getReadFields() {
        return readFields;
    }

    public Set<MethodRef> getVirtualCalls() {
        return virtualCalls;
    }

    public Set<MethodRef> getExactCalls() {
        return exactCalls;
    }
}
