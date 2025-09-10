package com.nucleonforge.axile.common.api.info.components;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * DTO that encapsulates the OS information of the given artifact.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/info.html">Info Endpoint</a>
 * @author Sergey Cherkasov
 */
public record OSInfo(
        @JsonProperty("name") String name,
        @JsonProperty("version") String version,
        @JsonProperty("arch") String arch) {}
