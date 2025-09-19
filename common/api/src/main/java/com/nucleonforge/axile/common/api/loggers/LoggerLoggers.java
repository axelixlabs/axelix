package com.nucleonforge.axile.common.api.loggers;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * DTO that encapsulates the logger information of the given artifact.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/loggers.html">Loggers Endpoint</a>
 * @author Sergey Cherkasov
 */
public record LoggerLoggers(
        @JsonProperty("configuredLevel") @Nullable String configuredLevel,
        @JsonProperty("effectiveLevel") String effectiveLevel) {}
