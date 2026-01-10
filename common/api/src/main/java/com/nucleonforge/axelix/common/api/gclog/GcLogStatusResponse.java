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
package com.nucleonforge.axelix.common.api.gclog;

import org.jspecify.annotations.Nullable;

/**
 * Response DTO representing the current status of garbage collection logging.
 *
 * @param enabled indicates whether GC logging is currently enabled (true) or disabled (false).
 * @param level The verbosity level of GC logging (e.g., "info", "debug", "trace").
 *              May be null if logging is disabled.
 *
 * @since 10.01.2026
 * @author Nikita Kirillov
 */
public record GcLogStatusResponse(boolean enabled, @Nullable String level) {}
