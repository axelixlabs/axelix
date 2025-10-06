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
