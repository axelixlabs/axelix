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

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axelix.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * DTO that encapsulates the process information of the given artifact.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/info.html">Info Endpoint</a>
 * @author Sergey Cherkasov
 */
public record ProcessInfo(
        @JsonProperty("pid") Long pid,
        @JsonProperty("parentPid") Long parentPid,
        @JsonProperty("owner") String owner,
        @JsonProperty("memory") @Nullable Memory memory,
        @JsonProperty("virtualThreads") @Nullable VirtualThreads virtualThreads,
        @JsonProperty("cpus") Integer cpus) {

    public record Memory(
            @JsonProperty("heap") @Nullable Heap heap,
            @JsonProperty("nonHeap") @Nullable NonHeap nonHeap,
            @JsonProperty("garbageCollectors") @Nullable Set<GarbageCollectors> garbageCollectors) {

        public record Heap(
                @JsonProperty("max") Long max,
                @JsonProperty("used") Long used,
                @JsonProperty("committed") Long committed,
                @JsonProperty("init") Long init) {}

        public record NonHeap(
                @JsonProperty("max") Long max,
                @JsonProperty("used") Long used,
                @JsonProperty("committed") Long committed,
                @JsonProperty("init") Long init) {}

        public record GarbageCollectors(
                @JsonProperty("name") String name, @JsonProperty("collectionCount") Integer collectionCount) {}
    }

    public record VirtualThreads(
            @JsonProperty("mounted") Long mounted,
            @JsonProperty("queued") Long queued,
            @JsonProperty("parallelism") Long parallelism,
            @JsonProperty("poolSize") Long poolSize) {}
}
