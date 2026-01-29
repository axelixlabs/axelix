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
package com.axelixlabs.axelix.sbs.spring.utils;

import org.jspecify.annotations.Nullable;

/**
 * Utilities needed to work with {@link String} objects.
 *
 * @author Mikhail Polivakha
 */
public class StringUtils {

    public static boolean containsIgnoreCase(@Nullable String source, @Nullable String destination) {

        if (source == null) {
            return destination == null;
        }

        if (destination == null) {
            return false;
        }

        if (source.length() < destination.length()) {
            return false;
        }

        int srcIndex = 0, destIndex = 0;

        for (; srcIndex < source.toCharArray().length; ) {
            char sourceChar = source.charAt(srcIndex + destIndex);

            char thisUpper = Character.toUpperCase(sourceChar);
            char thatUpper = Character.toUpperCase(destination.charAt(destIndex));

            if (thisUpper == thatUpper) {
                destIndex++;

                if (destIndex == destination.length()) {
                    return true;
                }
            } else {
                srcIndex++;
                destIndex = 0;
            }
        }

        return false;
    }

    public static String emptyIfNull(String target) {
        return target != null ? target : "";
    }
}
