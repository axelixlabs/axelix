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

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.jspecify.annotations.Nullable;

/**
 * Classification of the failed probe responses.
 *
 * @author Marsel Semenov
 */
public final class ProbeVerdicts {

    private ProbeVerdicts() {}

    /**
     * @param status the HTTP status of a failed probe, or {@code null} if no response was received
     * @return the verdict, see {@link ProbeVerdict} for the mapping
     */
    public static ProbeVerdict fromStatus(@Nullable Integer status) {
        if (status == null) {
            return ProbeVerdict.TRANSIENT;
        }
        if (status == 429) {
            return ProbeVerdict.THROTTLED;
        }
        if (status == 408 || status >= 500) {
            return ProbeVerdict.TRANSIENT;
        }
        if (status >= 200 && status < 300) {
            // 2xx with an unreadable body: the endpoint answered, but not as an Axelix starter
            return ProbeVerdict.REJECTED;
        }
        // 3xx (the JDK client does not follow redirects) and 4xx
        return ProbeVerdict.PERMANENT;
    }

    /**
     * @param header the raw value of the {@code Retry-After} header, either delay seconds or an HTTP date
     * @return the delay to wait, {@link Duration#ZERO} for a date in the past, or {@code null} if the header
     *         is missing or malformed
     */
    public static @Nullable Duration parseRetryAfter(@Nullable String header, Instant now) {
        if (header == null || header.isBlank()) {
            return null;
        }
        try {
            return Duration.ofSeconds(Long.parseLong(header.trim()));
        } catch (NumberFormatException ignored) {
            try {
                Instant at = ZonedDateTime.parse(header.trim(), DateTimeFormatter.RFC_1123_DATE_TIME)
                        .toInstant();
                return at.isAfter(now) ? Duration.between(now, at) : Duration.ZERO;
            } catch (DateTimeParseException e) {
                return null;
            }
        }
    }
}
