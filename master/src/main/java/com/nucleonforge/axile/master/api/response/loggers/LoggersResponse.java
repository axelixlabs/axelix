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
package com.nucleonforge.axile.master.api.response.loggers;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

/**
 * The profile of the logging system.
 *
 * @param levels     The levels supported by the current logging system.
 * @param groups     The logger groups keyed by name.
 * @param loggers    The loggers keyed by name.
 *
 * @author Sergey Cherkasov
 */
public record LoggersResponse(List<String> levels, List<Group> groups, List<Logger> loggers) {

    /**
     * The profile of a group with its loggers.
     *
     * @param name              The name of the logger group.
     * @param configuredLevel   The configured level of the logger group, if any.
     * @param members           The loggers that are part of this group.
     *
     * @author Sergey Cherkasov
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Group(String name, @Nullable String configuredLevel, List<String> members) {}

    /**
     * Profile of the active logger.
     *
     * @param name             The name of the logger.
     * @param configuredLevel  The configured logger level, if any.
     * @param effectiveLevel   The logger's current level.
     *
     * @author Sergey Cherkasov
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Logger(String name, @Nullable String configuredLevel, String effectiveLevel) {}
}
