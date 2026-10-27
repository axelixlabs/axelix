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

import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.Nullable;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Custom {@link MappingJackson2HttpMessageConverter} dedicated to Axelix payloads,
 * isolating serialization and deserialization from the host application's Jackson configuration.
 *
 * @author Darshan A S
 */
public class AxelixJacksonHttpMessageConverter extends MappingJackson2HttpMessageConverter {

    public AxelixJacksonHttpMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public boolean canRead(Class<?> clazz, @Nullable MediaType mediaType) {
        return isAxelixPayload(clazz) && super.canRead(clazz, mediaType);
    }

    @Override
    public boolean canRead(Type type, @Nullable Class<?> contextClass, @Nullable MediaType mediaType) {
        return isAxelixPayload(type) && super.canRead(type, contextClass, mediaType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, @Nullable MediaType mediaType) {
        return isAxelixPayload(clazz) && super.canWrite(clazz, mediaType);
    }

    @Override
    public boolean canWrite(@Nullable Type type, Class<?> clazz, @Nullable MediaType mediaType) {
        return isAxelixPayload(clazz) && super.canWrite(type, clazz, mediaType);
    }

    private static boolean isAxelixPayload(@Nullable Type type) {
        if (type instanceof Class<?>) {
            return isAxelixPayload((Class<?>) type);
        }
        return false;
    }

    private static boolean isAxelixPayload(@Nullable Class<?> clazz) {
        return clazz != null && clazz.getName().startsWith("com.axelixlabs.axelix.");
    }
}
