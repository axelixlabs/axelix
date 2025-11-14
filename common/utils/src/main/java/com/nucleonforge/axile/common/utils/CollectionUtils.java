package com.nucleonforge.axile.common.utils;

import java.util.Set;

/**
 * Utils for working with Java Collection Framework.
 *
 * @author Mikhail Polivakha
 */
public class CollectionUtils {

    public static <T> Set<T> defaultIfEmpty(Set<T> source, T defaultValue) {
        if (source == null || source.isEmpty()) {
            return Set.of(defaultValue);
        } else {
            return source;
        }
    }
}
