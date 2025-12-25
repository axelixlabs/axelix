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

import java.util.Set;

/**
 * SPI interface of a Role. A role is comprised from a set of {@link Authority authorities}.
 *
 * @see Authority
 * @since 16.07.25
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
public interface Role {

    /**
     * The unique name of this role.
     *
     * @return the name of the role.
     */
    String name();

    /**
     * Authorities of a given role.
     *
     * @return immutable set of {@link Authority} objects associated with this role
     */
    Set<Authority> authorities();

    /**
     * Component roles that are included in this role.
     * <p>
     * This allows defining hierarchical roles. The hierarchy must form a
     * <strong>directed acyclic graph (DAG)</strong>.
     * Implementations must ensure there are no duplicate or cyclic roles within the hierarchy.
     *
     * @return immutable set of {@link Role} objects included in this role
     */
    Set<Role> components();
}
