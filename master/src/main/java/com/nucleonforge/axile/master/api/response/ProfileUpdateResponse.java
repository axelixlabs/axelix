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
package com.nucleonforge.axile.master.api.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.nucleonforge.axile.common.api.ProfileMutationResult;

/**
 * The response of a profile update operation in the application.
 *
 * @param updated indicates whether the profiles were successfully updated
 * @param reason  the reason describing why the update was applied or skipped
 *
 * @see ProfileMutationResult
 * @since 25.09.2025
 * @author Nikita Kirillov
 */
public record ProfileUpdateResponse(
        @Schema(description = "Indicates whether the profiles were successfully updated", example = "true")
                boolean updated,
        @Schema(
                        description = "The reason describing why the update was applied or skipped (may be null)",
                        nullable = true,
                        example = "Profiles replaced successfully")
                String reason) {}
