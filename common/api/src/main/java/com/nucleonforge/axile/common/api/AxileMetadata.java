package com.nucleonforge.axile.common.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the metadata of a service instance as exposed by the Axile SBS actuator endpoint.
 *
 * @param groupId the group ID of the service (typically the Maven/Gradle group)
 * @param version the version of the service as declared in its build metadata
 *
 * @since 18.09.2025
 * @author Nikita Kirillov
 */
public record AxileMetadata(@JsonProperty("groupId") String groupId, @JsonProperty("version") String version) {}
