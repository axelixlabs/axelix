package com.nucleonforge.axile.common.api.env;

import org.jspecify.annotations.Nullable;

/**
 * The value of a property within a property source.
 *
 * @param value  the string representation of the property's value
 * @param origin the origin of the property if available (e.g. location in a file), may be {@code null}
 *
 * @see EnvironmentProperty
 * @since 03.09.2025
 * @author Nikita Kirillov
 */
public record PropertyValue(String value, @Nullable String origin) {}
