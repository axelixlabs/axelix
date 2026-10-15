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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Backoff of the auto-discovery probes.
 *
 * @param timeout              the timeout of a single probe request, also used to size the lease
 * @param minLease             the minimum time an instance stays claimed by the replica probing it
 * @param transientBase        the delay after the first {@code TRANSIENT} failure, doubled after each next one
 * @param transientCap         the maximum delay after {@code TRANSIENT} failures
 * @param throttledDefault     the delay after a {@code THROTTLED} failure without a {@code Retry-After} header
 * @param throttledCap         the maximum delay after a {@code THROTTLED} failure, even if asked for more
 * @param permanent            the delay after a {@code PERMANENT} or {@code REJECTED} failure
 * @param maxTransientFailures the number of consecutive {@code TRANSIENT}/{@code THROTTLED} failures after which
 *                             a registered instance is removed from the registry
 * @author Marsel Semenov
 */
@ConfigurationProperties("axelix.master.discovery.probe")
public record ProbeBackoffProperties(
        @DefaultValue("5s") Duration timeout,
        @DefaultValue("1m") Duration minLease,
        @DefaultValue("1m") Duration transientBase,
        @DefaultValue("30m") Duration transientCap,
        @DefaultValue("5m") Duration throttledDefault,
        @DefaultValue("1h") Duration throttledCap,
        @DefaultValue("1h") Duration permanent,
        @DefaultValue("5") int maxTransientFailures) {}
