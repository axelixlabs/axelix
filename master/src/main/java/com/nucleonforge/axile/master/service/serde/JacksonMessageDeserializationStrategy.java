/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.master.service.serde;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NonNull;

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
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    /**
     * @return the instance of the {@link Class} that this {@link JacksonMessageDeserializationStrategy} supports.
     */
    public abstract @NonNull Class<T> supported();
}
