package com.nucleonforge.axile.sbs.spring.profiles;

/**
 * The response to profile mutation request.
 *
 * @param updated true if profiles were updated successfully, false otherwise
 * @param reason  reason message explaining failure or null if successful
 *
 * @since 11.07.2025
 * @author Nikita Kirillov
 */
public record ProfileMutationResponse(boolean updated, String reason) {}
