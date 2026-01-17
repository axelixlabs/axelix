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
package com.nucleonforge.axelix.common.api.info.components;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axelix.common.domain.spring.actuator.ActuatorEndpoint;

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
