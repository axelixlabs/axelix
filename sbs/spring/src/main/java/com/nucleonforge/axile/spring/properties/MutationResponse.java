package com.nucleonforge.axile.spring.properties;

/**
 * The response to property mutation request
 *
 * @param mutated true if mutated successfully, false otherwise
 * @param reason reason why mutation
 * @since 04.07.25
 * @author Mikhail Polivakha
 */
public record MutationResponse(boolean mutated, String reason) {}
