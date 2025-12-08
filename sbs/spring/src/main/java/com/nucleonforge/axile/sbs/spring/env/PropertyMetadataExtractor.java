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
package com.nucleonforge.axile.sbs.spring.env;

import org.jspecify.annotations.Nullable;

/**
 * Provides access to Spring Boot property metadata information.
 *
 * @since 04.12.2025
 * @author Nikita Kirillov
 */
public interface PropertyMetadataExtractor {

    /**
     * Returns metadata for the specified property, or {@code null} if not found.
     */
    @Nullable
    PropertyMetadata getMetadata(String propertyName);
}
