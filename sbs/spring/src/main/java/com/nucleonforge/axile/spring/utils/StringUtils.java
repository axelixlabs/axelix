package com.nucleonforge.axile.spring.utils;

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
}
