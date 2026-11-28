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

import org.jspecify.annotations.NullMarked;

/**
 * Converts JWT parts between JSON bytes and Java objects.
 *
 * <p>The JWT support in common is not tied to any particular JSON library. The starters and master
 * provide an implementation backed by the JSON library available on their classpath.</p>
 *
 * @author Artemiy Degtyarev
 */
@NullMarked
public interface JwtJsonCodec {

    /**
     * @param value object to serialize, e.g. a JWT contract model
     * @return UTF-8 encoded JSON
     * @throws IllegalArgumentException if the value cannot be serialized
     */
    byte[] write(Object value) throws IllegalArgumentException;

    /**
     * @param json UTF-8 encoded JSON object
     * @return the members of the JSON object, nested objects are represented as {@link Map}s
     *         and arrays as {@link java.util.List}s
     * @throws IllegalArgumentException if the input is not a valid JSON object
     */
    Map<String, Object> read(byte[] json) throws IllegalArgumentException;
}
