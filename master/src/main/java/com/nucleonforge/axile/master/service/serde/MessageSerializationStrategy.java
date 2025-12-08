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

import org.jspecify.annotations.NonNull;

/**
 * The strategy for serializing any given object into byte array.
 *
 * @author Mikhail Polivakha
 */
public interface MessageSerializationStrategy {

    /**
     * Perform actual serialization.
     *
     * @param object the object to be serialized.
     * @throws SerializationException in case the error happened during serialization.
     * @return the serialized version of an object.
     */
    byte @NonNull [] serialize(@NonNull Object object) throws SerializationException;
}
