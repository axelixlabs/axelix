/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
