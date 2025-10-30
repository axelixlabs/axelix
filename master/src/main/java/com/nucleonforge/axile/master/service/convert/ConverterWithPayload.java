package com.nucleonforge.axile.master.service.convert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Interface that is capable to convert values from type {@code S} to type {@code T}, methods that accept a payload.
 *
 * @param <S> the source type
 * @param <T> the target type
 */
public interface ConverterWithPayload<S, T> {
    default @Nullable T convert(@Nullable S source, @NonNull String payload) {
        return source == null ? null : convertInternal(source, payload);
    }

    @NonNull
    T convertInternal(@NonNull S source, @NonNull String payload);

    default @NonNull Collection<@Nullable T> convertAll(
            @NonNull Collection<@Nullable S> sources, @NonNull String payload) {
        List<@Nullable T> result = new ArrayList<>();

        for (S source : sources) {
            result.add(convert(source, payload));
        }

        return result;
    }
}
