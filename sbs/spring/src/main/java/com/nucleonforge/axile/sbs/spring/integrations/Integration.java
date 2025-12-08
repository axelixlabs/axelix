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
package com.nucleonforge.axile.sbs.spring.integrations;

import java.util.HashMap;
import java.util.Map;

/**
 * The integration that service has with some other entity on the network
 *
 * @since 05.07.25
 * @author Mikhail Polivakha
 */
public sealed interface Integration permits AbstractIntegration {

    /**
     * @return abstract term that defines the type of entity with which the integration takes place
     */
    String entityType();

    /**
     * Protocol being used for communication
     */
    String protocol();

    /**
     * @return network address being used inside the app for communicating with this entity
     */
    String networkAddress();

    /**
     * @return key-value pairs, that represent some properties, that are specific to this integration or integration entity
     */
    default Map<String, Object> properties() {
        return new HashMap<>(0);
    }
}
