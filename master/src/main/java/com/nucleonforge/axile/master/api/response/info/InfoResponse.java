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
package com.nucleonforge.axile.master.api.response.info;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.master.api.response.info.components.BuildProfile;
import com.nucleonforge.axile.master.api.response.info.components.GitProfile;
import com.nucleonforge.axile.master.api.response.info.components.JavaProfile;
import com.nucleonforge.axile.master.api.response.info.components.OSProfile;
import com.nucleonforge.axile.master.api.response.info.components.ProcessProfile;
import com.nucleonforge.axile.master.api.response.info.components.SSLProfile;

/**
 * The profile of a given info.
 *
 * @param git     The short profile of the git component response, if available.
 * @param build   The short profile of the build component response, if available.
 * @param os      The short profile of the OS component response, if available.
 * @param process The short profile of the process component response, if available.
 * @param java    The short profile of the java component response, if available.
 * @param ssl     The short profile of the SSL component response, if available (present since version 3.4.9).
 *
 * @author Sergey Cherkasov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record InfoResponse(
        @Nullable GitProfile git,
        @Nullable BuildProfile build,
        @Nullable OSProfile os,
        @Nullable ProcessProfile process,
        @Nullable JavaProfile java,
        @Nullable SSLProfile ssl) {}
