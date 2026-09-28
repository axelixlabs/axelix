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
package com.axelixlabs.axelix.master.service.auth;

import java.io.OutputStream;
import java.io.Reader;
import java.util.Map;

import io.jsonwebtoken.io.AbstractDeserializer;
import io.jsonwebtoken.io.AbstractSerializer;
import io.jsonwebtoken.io.Deserializer;
import io.jsonwebtoken.io.Serializer;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import com.axelixlabs.axelix.common.auth.service.JwtJsonEngine;

/**
 * {@link JwtJsonEngine} backed by Jackson 3 {@link ObjectMapper}.
 *
 * @author Nikita Kirillov
 */
public class JacksonJwtJsonEngine implements JwtJsonEngine {

    private final ObjectMapper objectMapper;

    public JacksonJwtJsonEngine(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Serializer<Map<String, ?>> serializer() {
        return new AbstractSerializer<>() {
            @Override
            protected void doSerialize(Map<String, ?> value, OutputStream out) throws JacksonException {
                objectMapper.writeValue(out, value);
            }
        };
    }

    @Override
    public Deserializer<Map<String, ?>> deserializer() {
        return new AbstractDeserializer<>() {
            @Override
            protected Map<String, ?> doDeserialize(Reader reader) throws JacksonException {
                return objectMapper.readValue(reader, new TypeReference<Map<String, Object>>() {});
            }
        };
    }
}
