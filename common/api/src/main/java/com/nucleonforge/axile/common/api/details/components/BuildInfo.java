package com.nucleonforge.axile.common.api.details.components;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO that encapsulates the build information of the given artifact.
 *
 * @param artifact     The artifact ID of the application.
 * @param version      The version of the application.
 * @param group        The group ID of the application.
 * @param time         The time the application was built.
 *
 * @author Sergey Cherkasov
 */
public record BuildInfo(
        @JsonProperty("artifact") String artifact,
        @JsonProperty("name") String name,
        @JsonProperty("version") String version,
        @JsonProperty("group") String group,
        @JsonProperty("time") String time) {}
