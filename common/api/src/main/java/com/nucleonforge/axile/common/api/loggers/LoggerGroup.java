package com.nucleonforge.axile.common.api.loggers;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * DTO that encapsulates the logging level information of the loggers group.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/loggers.html">Loggers Endpoint</a>
 * @author Sergey Cherkasov
 */
public record LoggerGroup(
        @JsonProperty("configuredLevel") @Nullable String configuredLevel,
        @JsonProperty("members") List<String> members) {}
