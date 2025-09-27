package com.nucleonforge.axile.common.api;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response to profile-management actuator endpoint.
 *
 * @see ActuatorEndpoint
 * @since 24.09.2025
 * @author Nikita Kirillov
 */
public record ProfileMutationResult(boolean updated, String reason) {}
