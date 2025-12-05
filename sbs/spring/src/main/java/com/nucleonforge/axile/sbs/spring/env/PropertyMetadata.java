package com.nucleonforge.axile.sbs.spring.env;

import org.jspecify.annotations.Nullable;

/**
 * Metadata for a Spring Boot property, including description and deprecation info.
 *
 * @param description the property description
 * @param deprecated whether the property is deprecated
 * @param deprecatedReason the reason for deprecation, if any
 * @param deprecatedReplacement the replacement property name, if any
 *
 * @since 04.12.2025
 * @author Nikita Kirillov
 */
public record PropertyMetadata(
        @Nullable String description,
        boolean deprecated,
        @Nullable String deprecatedReason,
        @Nullable String deprecatedReplacement) {}
