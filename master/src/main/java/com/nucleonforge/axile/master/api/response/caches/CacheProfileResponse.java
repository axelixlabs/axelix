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
package com.nucleonforge.axile.master.api.response.caches;

/**
 * The profile contains details about the requested cache.
 *
 * @param name            The cache name.
 * @param target          The fully qualified name of the native cache.
 * @param cacheManager    The cache manager name.
 *
 * @author Sergey Cherkasov
 */
public record CacheProfileResponse(String name, String target, String cacheManager) {}
