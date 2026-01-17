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

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import com.nucleonforge.axelix.common.api.BeansFeed.BeanSource;

/**
 * Custom {@link JsonDeserializer} for the {@link BeanSource}.
 *
 * @author Nikita Kirillov
 */
public class BeanSourceDeserializer extends JsonDeserializer<BeanSource> {

    public static final String ORIGIN_FIELD = "origin";

    @Override
    public BeanSource deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = p.getCodec().readTree(p);

        return switch (BeansFeed.BeanOrigin.valueOf(node.get(ORIGIN_FIELD).asText())) {
            case COMPONENT_ANNOTATION -> new BeansFeed.ComponentVariant();
            case BEAN_METHOD -> ctxt.readTreeAsValue(node, BeansFeed.BeanMethod.class);
            case FACTORY_BEAN -> ctxt.readTreeAsValue(node, BeansFeed.FactoryBean.class);
            case SYNTHETIC_BEAN -> ctxt.readTreeAsValue(node, BeansFeed.SyntheticBean.class);
            case UNKNOWN -> new BeansFeed.UnknownBean();
        };
    }
}
