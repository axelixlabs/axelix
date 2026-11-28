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
package com.axelixlabs.axelix.sbs.spring.core.auth;

import java.util.Map;

import org.jspecify.annotations.NullMarked;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import com.axelixlabs.axelix.common.auth.service.JwtJsonCodec;

/**
 * {@link JwtJsonCodec} backed by Jackson 3.
 *
 * @author Artemiy Degtyarev
 */
@NullMarked
public class Jackson3JwtJsonCodec implements JwtJsonCodec {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Override
    public byte[] write(Object value) throws IllegalArgumentException {
        try {
            return jsonMapper.writeValueAsBytes(value);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Failed to serialize JWT part to JSON", e);
        }
    }

    @Override
    public Map<String, Object> read(byte[] json) throws IllegalArgumentException {
        try {
            Map<String, Object> result = jsonMapper.readValue(json, MAP_TYPE);
            if (result == null) {
                throw new IllegalArgumentException("JWT part is not a JSON object");
            }
            return result;
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Failed to parse JWT part as JSON object", e);
        }
    }
}
