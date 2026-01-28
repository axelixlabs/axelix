/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.common.api.info;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.api.info.components.BuildInfo;
import com.axelixlabs.axelix.common.api.info.components.GitInfo;
import com.axelixlabs.axelix.common.api.info.components.JavaInfo;
import com.axelixlabs.axelix.common.api.info.components.OSInfo;
import com.axelixlabs.axelix.common.api.info.components.ProcessInfo;
import com.axelixlabs.axelix.common.api.info.components.SSLInfo;
import com.axelixlabs.axelix.common.domain.spring.actuator.ActuatorEndpoint;

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
