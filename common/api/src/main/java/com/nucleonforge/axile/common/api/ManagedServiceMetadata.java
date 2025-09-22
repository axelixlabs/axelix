package com.nucleonforge.axile.common.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the metadata of a service instance as exposed by the Axile SBS actuator endpoint.
 *
 * @param version the version of the service as declared in its build metadata
 *
 * @since 18.09.2025
 * @author Nikita Kirillov
 */
public record ManagedServiceMetadata(@JsonProperty("version") String version) {}
