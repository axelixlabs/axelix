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
package com.axelixlabs.axelix.master.service.discovery.probe.backoff;

import java.time.Duration;
import java.time.Instant;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.master.service.discovery.probe.ProbeOutcome;

/**
 * Computes when an instance should be probed next, depending on the outcome of the latest probe.
 *
 * @author Marsel Semenov
 */
@Component
@EnableConfigurationProperties(ProbeBackoffProperties.class)
public class ProbeBackoff {

    private final ProbeBackoffProperties properties;

    public ProbeBackoff(ProbeBackoffProperties properties) {
        this.properties = properties;
    }

    /**
     * @param outcome  the outcome of the latest probe
     * @param failures the number of consecutive failures, including the latest probe; {@code 0} after a success
     * @return {@code now} after a success, so that the instance is probed again on the next run, and a moment
     *         delayed according to {@link ProbeBackoffProperties} after a failure
     */
    public Instant nextAttempt(ProbeOutcome outcome, int failures, Instant now) {
        return switch (outcome) {
            case ProbeOutcome.Success _ -> now;
            case ProbeOutcome.Failure f -> now.plus(delay(f, failures));
        };
    }

    private Duration delay(ProbeOutcome.Failure failure, int failures) {
        return switch (failure.verdict()) {
            case TRANSIENT -> exponential(properties.transientBase(), failures, properties.transientCap());
            case THROTTLED ->
                min(
                        failure.retryAfter() != null ? failure.retryAfter() : properties.throttledDefault(),
                        properties.throttledCap());
            case PERMANENT, REJECTED -> properties.permanent();
            case SUCCESS -> throw new IllegalArgumentException("unreachable");
        };
    }

    private static Duration exponential(Duration base, int failures, Duration cap) {
        int shift = Math.min(Math.max(failures - 1, 0), 20);
        Duration d = base.multipliedBy(1L << shift);
        return min(d, cap);
    }

    private static Duration min(Duration a, Duration b) {
        return a.compareTo(b) <= 0 ? a : b;
    }
}
