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
package com.nucleonforge.axile.common.domain.utils;

import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Lazily resolved value.
 * <p>
 * Inspired by <a href="https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/util/Lazy.html">Spring Data's Lazy</a>.
 *
 * @author Mikhail Polivakha
 */
public class Lazy<T> {

    private @Nullable T value;
    private @Nullable Supplier<T> supplier;
    private boolean resolved;

    private Lazy(@NonNull Supplier<T> supplier) {
        this.supplier = supplier;
    }

    private Lazy(@Nullable T value) {
        this.value = value;
        this.resolved = true;
    }

    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    public static <T> Lazy<T> resolved(@Nullable T value) {
        return new Lazy<>(value);
    }

    @SuppressWarnings("NullAway")
    public @Nullable T get() {
        if (!resolved) {
            value = supplier.get();
            resolved = true;
        }
        return value;
    }
}
