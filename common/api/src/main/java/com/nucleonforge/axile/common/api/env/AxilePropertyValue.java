package com.nucleonforge.axile.common.api.env;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * Represents a property value returned by the custom Axile environment endpoint.
 *
 * @param value                the string representation of the property's value
 * @param origin               the origin of the property if available (e.g. location in a file), may be {@code null}
 * @param isPrimary            whether this property value is primary (i.e. this value takes precedence over the other values
 *                             from other property sources)
 * @param configPropsBeanName  the name of the configprops (if any) bean onto which this property maps,
 *          *                  {@code null} otherwise
 *
 * @see EnvironmentFeed
 * @since 21.10.2025
 * @author Nikita Kirillov
 */
public record AxilePropertyValue(
        String value, @Nullable String origin, boolean isPrimary, @Nullable String configPropsBeanName) {

    @JsonCreator
    public AxilePropertyValue(
            @JsonProperty("value") String value,
            @JsonProperty("origin") @Nullable String origin,
            @JsonProperty("isPrimary") boolean isPrimary,
            @JsonProperty("configPropsBeanName") @Nullable String configPropsBeanName) {
        this.value = value;
        this.origin = origin;
        this.isPrimary = isPrimary;
        this.configPropsBeanName = configPropsBeanName;
    }
}
