package com.nucleonforge.axile.common.api.info.components;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * DTO that encapsulates the git information of the given artifact.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/info.html">Info Endpoint</a>
 * @author Sergey Cherkasov
 */
public record GitInfo(@JsonProperty("branch") String branch, @JsonProperty("commit") @Nullable Commit commit) {

    public record Commit(@JsonProperty("id") String id, @JsonProperty("time") String time) {}
}
