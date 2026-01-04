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
package com.nucleonforge.axelix.sbs.spring.auth;

import java.util.Optional;

import com.nucleonforge.axelix.common.auth.core.Authority;

/**
 * Interface for resolving a required {@link Authority}
 * based on the request path.
 *
 * @since 28.07.2025
 * @author Nikita Kirillov
 */
public interface AuthorityResolver {

    /**
     * Resolves the required {@link Authority} for the given request path.
     *
     * @param path the request path (e.g. "/actuator/axelix-beans")
     * @return an {@link Optional} containing the required {@link Authority},
     * or {@link Optional#empty()} if no authority is associated with the path
     */
    Optional<Authority> resolve(String path);
}
