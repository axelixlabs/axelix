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
import org.springframework.http.codec.json.JacksonJsonEncoder;
import org.springframework.util.MimeType;

/**
 * Custom {@link JacksonJsonEncoder} dedicated to Axelix payloads,
 * isolating serialization from the host application's Jackson configuration on WebFlux.
 *
 * @author Darshan A S
 * @author Nikita Kirillov
 */
public class AxelixJacksonEncoder extends JacksonJsonEncoder {

    public AxelixJacksonEncoder(JsonMapper jsonMapper) {
        super(jsonMapper);
    }

    @Override
    public boolean canEncode(ResolvableType elementType, @Nullable MimeType mimeType) {
        return isAxelixPayload(elementType) && super.canEncode(elementType, mimeType);
    }

    private static boolean isAxelixPayload(@Nullable ResolvableType elementType) {
        return elementType != null && elementType.toClass().getName().startsWith("com.axelixlabs.axelix.");
    }
}
