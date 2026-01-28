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
package com.axelixlabs.axelix.master.service.convert.utils;

import java.time.Duration;

import org.jspecify.annotations.NonNull;

/**
 * Utilities for formatting various Date/Time objects.
 *
 * @author Mikhail Polivakha
 */
public class DateTimeFormattingUtils {

    /**
     * Convert the passed {@code duration} to human-readable representation,
     * like: '2h 11m' or '3d 11h'
     *
     * @param duration duration to convert
     * @return converted duration
     */
    public static @NonNull String toHumanReadableDuration(@NonNull Duration duration) {
        StringBuilder durationAsString = new StringBuilder();
        long days = duration.toDays();

        if (days > 0) {
            return durationAsString
                    .append(days)
                    .append("d ")
                    .append(duration.minusDays(days).toHours())
                    .append("h")
                    .toString();
        } else {
            long hours = duration.toHours();

            if (hours > 0) {
                return durationAsString
                        .append(hours)
                        .append("h ")
                        .append(duration.minusHours(hours).toMinutes())
                        .append("m")
                        .toString();
            } else {
                long minutes = duration.toMinutes();

                return durationAsString
                        .append(minutes)
                        .append("m ")
                        .append(duration.minusMinutes(minutes).toSeconds())
                        .append("s")
                        .toString();
            }
        }
    }
}
