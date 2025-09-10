package com.nucleonforge.axile.common.api.info.components;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * DTO that encapsulates the build information of the given artifact.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/info.html">Info Endpoint</a>
 * @author Sergey Cherkasov
 */
public record BuildInfo(
        @JsonProperty("artifact") String artifact,
        @JsonProperty("name") String name,
        @JsonProperty("version") String version,
        @JsonProperty("group") String group,
        @JsonProperty("time") String time) {}
