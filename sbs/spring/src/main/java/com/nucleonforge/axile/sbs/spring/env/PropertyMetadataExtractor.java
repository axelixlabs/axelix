package com.nucleonforge.axile.sbs.spring.env;

import org.jspecify.annotations.Nullable;

/**
 * Provides access to Spring Boot property metadata information.
 *
 * @since 04.12.2025
 * @author Nikita Kirillov
 */
public interface PropertyMetadataExtractor {

    /**
     * Returns metadata for the specified property, or {@code null} if not found.
     */
    @Nullable
    PropertyMetadata getMetadata(String propertyName);
}
