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
package com.axelixlabs.axelix.master.service.discovery.probe.verdict;

/**
 * Classification of a probe outcome, which defines the backoff and whether the instance stays registered.
 *
 * @author Marsel Semenov
 */
public enum ProbeVerdict {

    /** The metadata endpoint responded, and the instance is manageable. */
    SUCCESS,

    /**
     * No response (connection refused, timeout, DNS failure), a 408 or 5xx response, or an unexpected error:
     * the instance may recover on its own.
     */
    TRANSIENT,

    /** 429 response: the instance is alive, but asks to back off. */
    THROTTLED,

    /**
     * 3xx (redirects are not followed) or 4xx response other than 408 and 429: most likely not an Axelix-enabled
     * service, which will not change without a redeployment.
     */
    PERMANENT,

    /**
     * 2xx response, but the instance is not manageable: unreadable metadata, incompatible starter version,
     * missing artifactId, or failed conversion to the internal representation.
     */
    REJECTED;

    public boolean isFailure() {
        return this != SUCCESS;
    }

    /**
     * Whether a previously registered instance should be kept in the registry after a failed probe.
     * A service that is temporarily unavailable or asks to back off is most likely still alive.
     *
     * @param consecutiveFailures  the number of consecutive failures, including the current one
     * @param maxTransientFailures the threshold after which a transient failure is no longer tolerated
     * @return {@code true} if the instance should remain registered
     */
    public boolean keepsRegistration(int consecutiveFailures, int maxTransientFailures) {
        return switch (this) {
            case SUCCESS -> true;
            case TRANSIENT, THROTTLED -> consecutiveFailures < maxTransientFailures;
            case PERMANENT, REJECTED -> false;
        };
    }
}
