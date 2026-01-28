/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.common.utils;

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

    public @NonNull T required() {
        T value = get();

        if (value == null) {
            throw new IllegalStateException("Expected a lazily-resolved value to be not null, but it is null");
        }

        return value;
    }
}
