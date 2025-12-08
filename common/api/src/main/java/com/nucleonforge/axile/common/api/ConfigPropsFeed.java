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
package com.nucleonforge.axile.common.api;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response to axile-configprops actuator endpoint.
 *
 * @param contexts  The application contexts keyed by context id.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/configprops.html">Сonfigprops Endpoint</a>
 *
 * @author Sergey Cherkasov
 */
public record ConfigPropsFeed(@JsonProperty("contexts") Map<String, Context> contexts) {

    /**
     * DTO that encapsulates the context of the given artifact.
     *
     * @param beans     The unified map of beans that contains beans from one or more contexts.
     *                  The key is the bean name (with potentially stripped config-props prefix), value is the profile of the given bean.
     * @param parentId  The id of the parent application context, if any.
     */
    public record Context(@JsonProperty("beans") Map<String, Bean> beans, @JsonProperty("parentId") String parentId) {}

    /**
     * DTO that encapsulates the {@code @ConfigurationProperties} bean of the given artifact.
     *
     * @param prefix       The prefix applied to the names of the bean properties.
     * @param properties   The properties of the bean as name-value pairs.
     * @param inputs       The origin and value of each configuration parameter
     *                     — which value was applied and from which source
     *                     — to configure a specific property.
     */
    public record Bean(
            @JsonProperty("prefix") String prefix,
            @JsonProperty("properties") List<KeyValue> properties,
            @JsonProperty("inputs") List<KeyValue> inputs) {}
}
