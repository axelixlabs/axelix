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
package com.nucleonforge.axile.common.auth.core;

import java.util.Collections;
import java.util.Set;

/**
 * Default {@link Role} backed by real {@link #authorities}.
 *
 * @see Role
 * @since 16.07.25
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
public record DefaultRole(String name, Set<Authority> authorities, Set<Role> components) implements Role {

    public DefaultRole {
        if (authorities == null) {
            authorities = Collections.emptySet();
        }
        if (components == null) {
            components = Collections.emptySet();
        }
    }
}
