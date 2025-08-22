package com.nucleonforge.axile.master.service.convert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Interface that is capable to convert values from type {@code S} to type {@code T}.
 *
 * @param <S> the source type
 * @param <T> the target type
 */
public interface Converter<S, T> {

    default @Nullable T convert(@Nullable S source) {
        if (source == null) {
            return null;
        }

        return convertInternal(source);
    }

    @NonNull
    T convertInternal(@NonNull S source);

    default @NonNull Collection<@Nullable T> convertAll(@NonNull Collection<@Nullable S> sources) {
        List<@Nullable T> result = new ArrayList<>();

        for (S source : sources) {
            result.add(convert(source));
        }

        return result;
    }
}
