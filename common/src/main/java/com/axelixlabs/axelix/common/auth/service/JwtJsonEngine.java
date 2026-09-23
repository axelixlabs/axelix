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

import java.util.Map;

import io.jsonwebtoken.io.Deserializer;
import io.jsonwebtoken.io.Serializer;

/**
 * SPI that supplies the JSON {@link Serializer}/{@link Deserializer}.
 * <p>
 * Each module-consumer provides its own {@link JwtJsonEngine}, backed by whichever Jackson it already depends on.
 *
 * @author Nikita Kirillov
 */
public interface JwtJsonEngine {

    /**
     * @return the serializer.
     */
    Serializer<Map<String, ?>> serializer();

    /**
     * @return the deserializer.
     */
    Deserializer<Map<String, ?>> deserializer();
}
