package com.nucleonforge.axile.spring.properties;

/**
 * Represents a request to update (mutate) a specific configuration property
 * in the application.
 *
 * @param propertyName the name of the property to update. Must not be {@code null}.
 * @param newValue the new value to assign to the property.  May be {@code null} or empty.
 *
 * @since 26.09.2025
 * @author Nikita Kirillov
 */
public record PropertyMutationRequest(String propertyName, String newValue) {}
