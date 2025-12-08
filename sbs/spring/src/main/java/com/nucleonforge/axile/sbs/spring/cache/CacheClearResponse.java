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
package com.nucleonforge.axile.sbs.spring.cache;

/**
 * Response wrapper that indicates the result of a cache clear operation.
 *
 * @param cleared {@code true} if the cache was successfully cleared, {@code false} otherwise.
 *
 * @since 26.06.2025
 * @author Nikita Kirillov
 */
public record CacheClearResponse(boolean cleared) {}
