package com.nucleonforge.axile.master.api.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.common.api.env.EnvironmentProperty;

/**
 * The response object that represents an environment property in the application.
 *
 * @param source the name of the property source from which this value was obtained
 * @param value the string representation of the property's value
 *
 * @see EnvironmentProperty
 * @since 27.08.2025
 * @author Nikita Kirillov
 */
public record EnvironmentPropertyResponse(String source, String value, List<PropertySource> propertySources) {

    public record PropertySource(String name, @Nullable Property property) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Property(String value, @Nullable String origin) {}
}
