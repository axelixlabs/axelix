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
