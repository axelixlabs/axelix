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
package com.nucleonforge.axile.master.api.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Represents a request to update a specific configuration property in a given application instance.
 *
 * @param propertyName the name of the property to update. Must not be {@code null}.
 * @param newValue newValue the new value to assign to the property. May be {@code null} or empty.
 *
 * @since 25.09.2025
 * @author Nikita Kirillov
 */
public record PropertyUpdatedRequest(
        @Schema(description = "The name of the property to update", example = "spring.jpa.show-sql")
                String propertyName,
        @Schema(description = "The new value of the property. May be null or empty", example = "true")
                String newValue) {}
