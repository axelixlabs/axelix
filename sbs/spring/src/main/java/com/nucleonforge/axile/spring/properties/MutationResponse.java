package com.nucleonforge.axile.spring.properties;

import org.jspecify.annotations.Nullable;

/**
 * The response to property mutation request
 *
 * @param mutated true if mutated successfully, false otherwise
 * @param reason why mutation failed, might be null
 * @since 04.07.25
 * @author Mikhail Polivakha
 */
public record MutationResponse(boolean mutated, @Nullable String reason) {}
