package com.nucleonforge.axile.spring.utils;

/**
 * Utilities needed to work with {@link String} objects.
 *
 * @author Mikhail Polivakha
 */
public class StringUtils {

    private StringUtils() {}

    public static boolean containsIgnoreCase(String source, String destination) {

        if (source == null) {
            return destination == null;
        }

        if (destination == null) {
            return false;
        }

        int destIndex = 0;

        for (char c : source.toCharArray()) {

            char thisUpper = Character.toUpperCase(c);
            char thatUpper = Character.toUpperCase(destination.charAt(destIndex));

            if (thisUpper == thatUpper) {
                destIndex++;

                if (destIndex == destination.length()) {
                    return true;
                }
            } else {
                destIndex = 0;
            }
        }

        return false;
    }

    public static String defaultIfBlank(String value, String defaultValue) {
        return value != null && !value.isBlank() ? value : defaultValue;
    }
}
