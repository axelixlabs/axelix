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
package com.nucleonforge.axile.master.api.response.info.components;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * The profile of a given build.
 *
 * @param artifact     The artifact ID of the application.
 * @param name         The name of the application.
 * @param version      The version of the application.
 * @param group        The group ID of the application.
 * @param time         The time the application was built.
 *
 * @author Sergey Cherkasov
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BuildProfile(String artifact, String name, String version, String group, String time) {}
