/*
 * Copyright 2025-present the original author or authors.
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

import org.jspecify.annotations.NonNull;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

/**
 * {@link MessageDeserializationStrategy} for the deserialization of the {@code byte[]}
 * into the {@link Resource}.
 *
 * @since 12.11.2025
 * @author Nikita Kirillov
 */
public abstract class BinaryResourceMessageDeserializationStrategy implements MessageDeserializationStrategy<Resource> {

    @Override
    public @NonNull Resource deserialize(byte @NonNull [] binary) throws DeserializationException {
        try {
            return new ByteArrayResource(binary) {

                @Override
                public String getFilename() {
                    return filename();
                }
            };
        } catch (Exception e) {
            throw new DeserializationException(e);
        }
    }

    /**
     * @return the file name of the deserialized resource.
     */
    protected abstract String filename();
}
