package com.nucleonforge.axile.common.api.info.components;

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
public record JavaInfo(
        @JsonProperty("version") String version,
        @JsonProperty("vendor") @Nullable Vendor vendor,
        @JsonProperty("runtime") @Nullable Runtime runtime,
        @JsonProperty("jvm") @Nullable JVM jvm) {

    public record Vendor(@JsonProperty("name") String name, @JsonProperty("version") String version) {}

    public record Runtime(@JsonProperty("name") String name, @JsonProperty("version") String version) {}

    public record JVM(
            @JsonProperty("name") String name,
            @JsonProperty("vendor") String vendor,
            @JsonProperty("version") String version) {}
}
