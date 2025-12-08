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
package com.nucleonforge.axile.common.api.registration;

/**
 * Short information about the build of the given service. Provided during initial scan.
 *
 * @param buildTimestamp the timestamp when this application's build was created
 * @param serviceVersion the version of the <strong>managed service itself</strong>, i.e. the version
 *                of the end-service artifact (the V inside GAV coordinate). The assumption is that
 *                is never {@code null}, and it frankly should not be.
 *
 * @author Mikhail Polivakha
 */
public record ShortBuildInfo(String buildTimestamp, String serviceVersion) {}
