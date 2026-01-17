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
package com.nucleonforge.axelix.master.service.convert.response;

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
