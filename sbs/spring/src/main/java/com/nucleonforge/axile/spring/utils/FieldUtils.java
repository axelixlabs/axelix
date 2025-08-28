package com.nucleonforge.axile.spring.utils;

import java.lang.reflect.Field;

import org.springframework.util.ReflectionUtils;

/**
 * Utils for working with {@link Field} descriptors.
 *
 * @author Mikhail Polivakha
 */
public class FieldUtils {

    @SuppressWarnings({"unchecked", "NullAway"})
    public static <T> T getField(String name, Object onObject) {
        try {
            Field field = ReflectionUtils.findField(onObject.getClass(), name);
            if (field == null) {
                throw new NoSuchFieldException(name);
            }
            ReflectionUtils.makeAccessible(field);
            return (T) ReflectionUtils.getField(field, onObject);
        } catch (ClassCastException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
