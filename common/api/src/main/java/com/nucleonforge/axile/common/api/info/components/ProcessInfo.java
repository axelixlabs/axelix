/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.common.api.info.components;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

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
