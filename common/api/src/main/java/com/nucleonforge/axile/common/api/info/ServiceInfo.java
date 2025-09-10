package com.nucleonforge.axile.common.api.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.common.api.info.components.BuildInfo;
import com.nucleonforge.axile.common.api.info.components.GitInfo;
import com.nucleonforge.axile.common.api.info.components.JavaInfo;
import com.nucleonforge.axile.common.api.info.components.OSInfo;
import com.nucleonforge.axile.common.api.info.components.ProcessInfo;
import com.nucleonforge.axile.common.api.info.components.SSLInfo;
import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response to info actuator endpoint.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/info.html">Info Endpoint</a>
 * @author Sergey Cherkasov
 */
public record ServiceInfo(
        @JsonProperty("git") @Nullable GitInfo git,
        @JsonProperty("build") @Nullable BuildInfo build,
        @JsonProperty("os") @Nullable OSInfo os,
        @JsonProperty("process") @Nullable ProcessInfo process,
        @JsonProperty("java") @Nullable JavaInfo java,
        @JsonProperty("ssl") @Nullable SSLInfo ssl) {}
