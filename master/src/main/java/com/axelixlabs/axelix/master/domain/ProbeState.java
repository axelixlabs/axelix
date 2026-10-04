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
package com.axelixlabs.axelix.master.domain;

import java.time.Instant;

import org.jspecify.annotations.Nullable;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.axelixlabs.axelix.master.service.discovery.probe.verdict.ProbeVerdict;

/**
 * Probing state of a single auto-discovered instance. {@link #nextAttemptAt} serves both as the backoff after
 * failures and as the lease while some Master replica is probing the instance.
 *
 * @param version incremented on every claim and every recorded result, so that a result of a claim that has
 *                been taken over after its lease expired is not recorded
 *
 * @author Marsel Semenov
 */
@Table("probe_state")
public record ProbeState(
        @Id InstanceKey key,
        int consecutiveFailures,
        @Nullable ProbeVerdict lastVerdict,
        @Nullable Integer lastStatus,
        Instant nextAttemptAt,
        @Nullable Instant lastSuccessAt,
        long version) {

    public static ProbeState initial(InstanceKey key, Instant now) {
        return new ProbeState(key, 0, null, null, now, null, 0);
    }

    /**
     * The state as it is right after the claim, see {@code ProbeStateRepository#leaseDue}.
     */
    public ProbeState withLease(Instant leaseUntil) {
        return new ProbeState(
                key, consecutiveFailures, lastVerdict, lastStatus, leaseUntil, lastSuccessAt, version + 1);
    }

    /**
     * Keeps the {@link #version}, so that recording the result is checked against the version of the claim.
     */
    public ProbeState withResult(
            ProbeVerdict verdict, int failures, @Nullable Integer status, Instant next, Instant now) {
        return new ProbeState(
                key, failures, verdict, status, next, verdict == ProbeVerdict.SUCCESS ? now : lastSuccessAt, version);
    }

    public record InstanceKey(String serviceId, String host, int port) {}
}
