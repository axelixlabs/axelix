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
package com.axelixlabs.axelix.master.service.serde;

import org.jspecify.annotations.NonNull;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * {@link MessageDeserializationStrategy} based on Jackson.
 *
 * @author Mikhail Polivakha
 */
public abstract class JacksonMessageDeserializationStrategy<T> implements MessageDeserializationStrategy<T> {

    private final ObjectMapper objectMapper;

    protected JacksonMessageDeserializationStrategy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public @NonNull T deserialize(byte @NonNull [] binary) throws DeserializationException {
        try {
            return objectMapper.readValue(binary, supported());
        } catch (JacksonException e) {
            throw new DeserializationException(e);
        }
    }

    /**
     * @return the instance of the {@link Class} that this {@link JacksonMessageDeserializationStrategy} supports.
     */
    public abstract @NonNull Class<T> supported();
}
