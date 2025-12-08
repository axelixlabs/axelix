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
package com.nucleonforge.axile.sbs.spring.integrations;

/**
 * Base implementation of the {@link Integration} interface that provides common logic
 * and holds core integration parameters such as network address, protocol, and entity type.
 *
 * <p>This class is intended to be extended by specific types of integrations,
 * which may add additional behavior or properties as needed.</p>
 *
 * @since 05.07.2025
 * @author Mikhail Polivakha
 */
public abstract non-sealed class AbstractIntegration implements Integration {

    private final String networkAddress;
    private final String protocol;
    private final String entityType;

    protected AbstractIntegration(String networkAddress, String protocol, String entityType) {
        this.networkAddress = networkAddress;
        this.protocol = protocol;
        this.entityType = entityType;
    }

    @Override
    public String entityType() {
        return entityType;
    }

    @Override
    public String protocol() {
        return protocol;
    }

    @Override
    public String networkAddress() {
        return networkAddress;
    }
}
