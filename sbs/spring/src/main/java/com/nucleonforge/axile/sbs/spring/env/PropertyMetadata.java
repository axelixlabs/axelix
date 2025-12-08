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
package com.nucleonforge.axile.sbs.spring.env;

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
     * @param reason the reason why the given property is deprecated.
     * @param replacement the name of the property that potentially aims to replace the given deprecated property.
     */
    public record Deprecation(@Nullable String reason, @Nullable String replacement) {}
}
