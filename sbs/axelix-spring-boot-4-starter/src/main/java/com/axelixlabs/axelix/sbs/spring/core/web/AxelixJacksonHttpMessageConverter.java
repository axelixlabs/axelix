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
package com.axelixlabs.axelix.sbs.spring.core.web;

import org.jspecify.annotations.Nullable;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;

/**
 * Custom {@link JacksonJsonHttpMessageConverter} dedicated to Axelix payloads,
 * isolating serialization and deserialization from the host application's Jackson configuration.
 *
 * @author Darshan A S
 */
public class AxelixJacksonHttpMessageConverter extends JacksonJsonHttpMessageConverter {

    public AxelixJacksonHttpMessageConverter(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    public boolean canRead(ResolvableType type, @Nullable MediaType mediaType) {
        return isAxelixPayload(type.toClass()) && super.canRead(type, mediaType);
    }

    @Override
    public boolean canRead(Class<?> clazz, @Nullable MediaType mediaType) {
        return isAxelixPayload(clazz) && super.canRead(clazz, mediaType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType) {
        return isAxelixPayload(clazz) && super.canWrite(clazz, mediaType);
    }

    @Override
    public boolean canWrite(ResolvableType type, @Nullable Class<?> clazz, @Nullable MediaType mediaType) {
        Class<?> targetClass = clazz != null ? clazz : type.toClass();
        return isAxelixPayload(targetClass) && super.canWrite(type, targetClass, mediaType);
    }

    private static boolean isAxelixPayload(@Nullable Class<?> clazz) {
        return clazz != null && clazz.getName().startsWith("com.axelixlabs.axelix.");
    }
}
