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
package com.axelixlabs.axelix.master.service.discovery.probe;

import java.time.Duration;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.service.discovery.DiscoveredInstanceProfile;
import com.axelixlabs.axelix.master.service.discovery.probe.verdict.ProbeVerdict;

/**
 * The outcome of probing the metadata endpoint of a single instance.
 *
 * @author Marsel Semenov
 */
public sealed interface ProbeOutcome {

    ProbeVerdict verdict();

    /**
     * @param profile the instance and its metadata, ready to be registered
     */
    record Success(DiscoveredInstanceProfile profile) implements ProbeOutcome {
        @Override
        public ProbeVerdict verdict() {
            return ProbeVerdict.SUCCESS;
        }
    }

    /**
     * @param verdict    the classification of the failure, never {@link ProbeVerdict#SUCCESS}
     * @param status     the HTTP status of the response, or {@code null} if no response was received
     * @param retryAfter the delay the instance asked for via the {@code Retry-After} header, if any
     * @param reason     human-readable description of the failure, for logging
     */
    record Failure(
            ProbeVerdict verdict,
            @Nullable Integer status,
            @Nullable Duration retryAfter,
            String reason) implements ProbeOutcome {

        public Failure {
            if (verdict == ProbeVerdict.SUCCESS) {
                throw new IllegalArgumentException("Failure cannot carry SUCCESS verdict");
            }
        }
    }
}
