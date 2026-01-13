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
package com.nucleonforge.axelix.sbs.spring.scheduled;

/**
 * Represents a request to modify a scheduled task configuration.
 *
 * @param targetScheduledTask the identifier of the scheduled task to toggle. Must not be {@code null}.
 * @param newValue            the new value to be assigned.
 *
 * @author Sergey Cherkasov
 */
public record ScheduledTaskMutationRequest(String targetScheduledTask, String newValue) {}
