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
package com.nucleonforge.axile.sbs.spring.utils;

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
