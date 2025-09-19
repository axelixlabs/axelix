package com.nucleonforge.axile.common.api.loggers;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response to loggers actuator endpoint.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/loggers.html">Loggers Endpoint</a>
 * @author Sergey Cherkasov
 */
public record ServiceLoggers(
        @JsonProperty("levels") List<String> levels,
        @JsonProperty("loggers") Map<String, LoggerLoggers> loggers,
        @JsonProperty("groups") Map<String, GroupLoggers> groups) {}
