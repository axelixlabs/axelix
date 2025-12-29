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
package com.nucleonforge.axelix.sbs.spring.env;

import org.jspecify.annotations.Nullable;

/**
 * Metadata for a Spring Boot property, including description and deprecation info.
 *
 * @param description the property description.
 * @param deprecation deprecation related information. If {@code null}, the
 *                    property is not considered deprecated. If not {@code null},
 *                    then the property is considered deprecated.
 *
 * @since 04.12.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public record PropertyMetadata(@Nullable String description, @Nullable Deprecation deprecation) {

    /**
     * Deprecation metadata for a property.
     *
     * @param message explaining why the property is deprecated and, optionally, what should be used instead.
     */
    public record Deprecation(String message) {}
}
