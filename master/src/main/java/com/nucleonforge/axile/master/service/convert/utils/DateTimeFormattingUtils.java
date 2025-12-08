/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.master.service.convert.utils;

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
