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
package com.axelixlabs.axelix.common.auth.service;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * {@link JwtJsonCodec} backed by Jackson 2.
 *
 * @author Artemiy Degtyarev
 */
@NullMarked
public class Jackson2JwtJsonCodec implements JwtJsonCodec {

    private static final TypeReference<Map<String, @Nullable Object>> MAP_TYPE = new TypeReference<>() {};

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] write(Object value) throws IllegalArgumentException {
        try {
            return objectMapper.writeValueAsBytes(value);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to serialize JWT part to JSON", e);
        }
    }

    @Override
    public Map<String, @Nullable Object> read(byte[] json) throws IllegalArgumentException {
        try {
            Map<String, @Nullable Object> result = objectMapper.readValue(json, MAP_TYPE);
            //noinspection ConstantValue
            if (result == null) {
                throw new IllegalArgumentException("JWT part is not a JSON object");
            }
            return result;
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to parse JWT part as JSON object", e);
        }
    }
}
