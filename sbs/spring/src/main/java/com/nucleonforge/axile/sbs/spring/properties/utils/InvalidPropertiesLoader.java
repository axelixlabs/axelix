package com.nucleonforge.axile.sbs.spring.properties.utils;

import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * Loads configuration property validation rules.
 *
 * @since 28.11.2025
 * @author Nikita Kirillov
 */
public interface InvalidPropertiesLoader {

    /**
     * @return merged invalid properties with clients rules taking precedence.
     */
    Map<String, List<InvalidPropertyValue>> getInvalidProperties();

    /**
     * Checks if a property value is invalid.
     *
     * @param name property name
     * @param value value to validate
     * @return validation massage if value invalid, null otherwise
     */
    @Nullable
    String getInvalidPropertyValues(String name, @Nullable Object value);
}
