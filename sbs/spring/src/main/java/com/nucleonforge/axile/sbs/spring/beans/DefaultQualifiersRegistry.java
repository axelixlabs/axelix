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
package com.nucleonforge.axile.sbs.spring.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jspecify.annotations.NonNull;

/**
 * Default in-memory registry that stores the qualifiers of a given beans.
 *
 * @author Mikhail Polivakha
 */
public class DefaultQualifiersRegistry {

    public static final DefaultQualifiersRegistry INSTANCE = new DefaultQualifiersRegistry();

    private final ConcurrentMap<String, List<String>> cache;

    private DefaultQualifiersRegistry() {
        this.cache = new ConcurrentHashMap<>(32);
    }

    @NonNull
    public List<String> getQualifiers(@NonNull String beanName) {
        return Optional.ofNullable(cache.get(beanName)).orElse(new ArrayList<>());
    }

    public void registerQualifiers(@NonNull String beanName, @NonNull List<String> qualifiers) {
        cache.compute(beanName, (s, existingQualifiers) -> {
            if (existingQualifiers == null || existingQualifiers.isEmpty()) {
                existingQualifiers = new ArrayList<>();
            }
            existingQualifiers.addAll(qualifiers);
            return existingQualifiers;
        });
    }
}
