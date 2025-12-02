package com.nucleonforge.axile.sbs.spring.properties.utils;

/**
 * Represents an invalid property value with its validation description.
 *
 * @param value he invalid value that should be avoided.
 * @param description explanation why this value is invalid or problematic.
 *
 * @since 28.11.2025
 * @author Nikita Kirillov
 */
public record InvalidPropertyValue(Object value, String description) {}
