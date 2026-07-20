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
package com.axelixlabs.axelix.sbs.spring.core.persistence;

import com.axelixlabs.axelix.common.domain.insights.TypeExternalCall;

/**
 * Record of a single blocking call to an external system made while a transaction was open.
 *
 * @author Sergey Cherkasov
 */
public class SimpleExternalCallRecord {
    private final TypeExternalCall type;
    private final String target;
    private final long durationMs;

    /**
     * Create a new SimpleExternalCallRecord.
     *
     * @param type       the client that performed the call, e.g. {@link TypeExternalCall#HTTP_CLIENT} for an HTTP
     *                   call, {@link TypeExternalCall#KAFKA} for a messaging one.
     * @param target     where the call went: the request method and url for an HTTP call, e.g.
     *                   {@code "GET https://payments/charge"}. The topic / queue / exchange for a messaging call.
     * @param durationMs the call duration in milliseconds.
     */
    public SimpleExternalCallRecord(TypeExternalCall type, String target, long durationMs) {
        this.type = type;
        this.target = target;
        this.durationMs = durationMs;
    }

    public TypeExternalCall getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }

    public long getDurationMs() {
        return durationMs;
    }
}
