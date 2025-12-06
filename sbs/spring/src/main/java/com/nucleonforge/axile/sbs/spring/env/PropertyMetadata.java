package com.nucleonforge.axile.sbs.spring.env;

import org.jspecify.annotations.Nullable;

/**
 * Metadata for a Spring Boot property, including description and deprecation info.
 *
 * @param description the property description.
 * @param deprecation deprecation related information. If {@code null}, the
 *                    property is not considered deprecated. If not {@code null},
 *                    then the property is considered deprecated.
 *
 * @since 04.12.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public record PropertyMetadata(@Nullable String description, @Nullable Deprecation deprecation) {

    /**
     * @param reason the reason why the given property is deprecated.
     * @param replacement the name of the property that potentially aims to replace the given deprecated property.
     */
    public record Deprecation(@Nullable String reason, @Nullable String replacement) {}
}
