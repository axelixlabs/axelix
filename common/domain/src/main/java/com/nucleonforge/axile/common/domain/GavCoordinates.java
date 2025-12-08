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
package com.nucleonforge.axile.common.domain;

import org.jspecify.annotations.NonNull;

/**
 * The GAV coordinates of the dependency.
 *
 * @param groupId Group ID of the dependency.
 * @param artifactId Artifact ID of the dependency.
 * @param version Version of the dependency.
 *
 * @author Mikhail Polivakha
 */
public record GavCoordinates(@NonNull String groupId, @NonNull String artifactId, @NonNull String version) {}
