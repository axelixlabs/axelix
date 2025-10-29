package com.nucleonforge.axile.common.api.details.components;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * DTO that encapsulates the java information of the given artifact.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/info.html">Info Endpoint</a>
 * @author Sergey Cherkasov
 */
public record RuntimeDetails(
        @JsonProperty("javaVersion") String javaVersion,
        @JsonProperty("jdkVendor") String jdkVendor,
        @JsonProperty("garbageCollector") String garbageCollector,
        @JsonProperty("kotlinVersion") @Nullable String kotlinVersion) {}
