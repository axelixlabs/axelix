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
package com.nucleonforge.axile.sbs.spring.scheduled;

/**
 * Represents a request to toggle (enable/disable) a scheduled task.
 *
 * @param targetScheduledTask the identifier of the scheduled task to toggle. Must not be {@code null}.
 * @param force  if {@code true} and enabling - the task will start immediately regardless of its delay;
 *               if {@code true} and disabling - the task will be interrupted even if currently running.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 */
public record ScheduledTaskToggleRequest(String targetScheduledTask, boolean force) {}
