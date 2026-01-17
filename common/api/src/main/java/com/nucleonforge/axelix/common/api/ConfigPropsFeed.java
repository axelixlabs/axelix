/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.nucleonforge.axelix.common.api;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.nucleonforge.axelix.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * The response to axelix-configprops actuator endpoint.
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
