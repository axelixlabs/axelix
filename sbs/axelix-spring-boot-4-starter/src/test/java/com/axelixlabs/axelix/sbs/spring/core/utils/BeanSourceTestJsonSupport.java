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
package com.axelixlabs.axelix.sbs.spring.core.utils;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;

import com.axelixlabs.axelix.common.api.BeanSourceDeserializer;
import com.axelixlabs.axelix.common.api.BeansFeed;
import com.axelixlabs.axelix.common.api.BeansFeed.BeanSource;

/**
 * Test-only Jackson 3 support for deserializing the polymorphic {@link BeanSource}.
 *
 * <p>The shared {@code common/api} module carries a Jackson 2 {@link BeanSourceDeserializer} wired through
 * {@code @JsonDeserialize}, which the master (Jackson 2) uses to consume the beans feed. Spring Boot 4 ships Jackson 3,
 * whose {@code ValueDeserializer} contract the Jackson 2 deserializer does not satisfy, so the test client cannot
 * reconstruct the interface on its own. This mirrors that dispatch logic for Jackson 3 so the HTTP-level tests can
 * deserialize the response the endpoint produces.
 */
final class BeanSourceTestJsonSupport {

    private BeanSourceTestJsonSupport() {}

    /**
     * @return a Jackson 3 based message converter that is able to deserialize {@link BeanSource}.
     */
    static JacksonJsonHttpMessageConverter beanSourceAwareJsonConverter() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(BeanSource.class, new Jackson3BeanSourceDeserializer());

        JsonMapper mapper = JsonMapper.builder().addModule(module).build();
        return new JacksonJsonHttpMessageConverter(mapper);
    }

    private static final class Jackson3BeanSourceDeserializer extends ValueDeserializer<BeanSource> {

        @Override
        public BeanSource deserialize(JsonParser p, DeserializationContext ctxt) {
            JsonNode node = ctxt.readTree(p);

            switch (BeansFeed.BeanOrigin.valueOf(
                    node.get(BeanSourceDeserializer.ORIGIN_FIELD).asString())) {
                case COMPONENT_ANNOTATION:
                    return new BeansFeed.ComponentVariant();
                case BEAN_METHOD:
                    return ctxt.readTreeAsValue(node, BeansFeed.BeanMethod.class);
                case FACTORY_BEAN:
                    return ctxt.readTreeAsValue(node, BeansFeed.FactoryBean.class);
                case SYNTHETIC_BEAN:
                    return ctxt.readTreeAsValue(node, BeansFeed.SyntheticBean.class);
                case UNKNOWN:
                default:
                    return new BeansFeed.UnknownBean();
            }
        }
    }
}
