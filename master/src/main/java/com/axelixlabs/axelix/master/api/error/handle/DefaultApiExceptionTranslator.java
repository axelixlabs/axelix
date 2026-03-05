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
package com.axelixlabs.axelix.master.api.error.handle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.axelixlabs.axelix.master.api.error.ApiError;
import com.axelixlabs.axelix.master.api.error.handle.impl.DefaultExceptionHandler;

/**
 * The default implementation of the {@link ApiExceptionTranslator}. Also applies the caching
 * behavior, meaning, once concrete {@link ExceptionHandler} responsible for processing of a given
 * exception is found, then for this exact {@link Exception} class the bounded {@link ExceptionHandler}
 * is cached.
 *
 * @author Mikhail Polivakha
 */
@Component
public class DefaultApiExceptionTranslator implements ApiExceptionTranslator {

    private static final Logger log = LoggerFactory.getLogger(DefaultApiExceptionTranslator.class);

    private final ConcurrentMap<Class<?>, ExceptionHandler<?>> cache;

    private final List<ExceptionHandler<?>> exceptionHandlers;

    public DefaultApiExceptionTranslator(List<ExceptionHandler<?>> exceptionHandlers) {
        this.exceptionHandlers = exceptionHandlers;
        this.cache = new ConcurrentHashMap<>(32);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ApiError translateException(Exception e) {
        ExceptionHandler exceptionHandler = cache.computeIfAbsent(e.getClass(), this::deriveExceptionHandler);
        return exceptionHandler.handle(e);
    }

    private ExceptionHandler<?> deriveExceptionHandler(Class<?> targetClass) {

        List<Pair<Integer, ExceptionHandler<?>>> candidates = new ArrayList<>();

        for (ExceptionHandler<?> exceptionHandler : exceptionHandlers) {
            int inheritanceLevel = getInheritanceLevel(targetClass, exceptionHandler.supported());

            if (inheritanceLevel == 0) {
                return exceptionHandler;
            }

            if (inheritanceLevel > 0) {
                candidates.add(Pair.of(inheritanceLevel, exceptionHandler));
            }
        }

        if (candidates.isEmpty()) {
            log.info("""
                Did not found any specific exception handler for exception {}.
                It may be fine, but most probably it is not. Consider reporting this to maintainers
                """, targetClass.getSimpleName());
            return DefaultExceptionHandler.INSTANCE;
        }

        candidates.sort(Comparator.comparing(Pair::getLeft));
        return Objects.requireNonNull(CollectionUtils.firstElement(candidates)).getRight();
    }

    /**
     * Find out the amount of superclasses between {@code source} and {@code superType}.
     *
     * @apiNote This method does NOT account for interfaces.
     * @param source    the class from which we start the traversal to the top.
     * @param superType the potential parent of the {@code source}.
     * @return 0 in case {@code source == superType}, -1 in case {@code superType} is not a
     * superclass of the {@code source}, or the positive integer
     * indicating the amount of intermediate classes/interfaces + 1 between
     * {@code source} and {@code superType}.
     */
    private int getInheritanceLevel(@NonNull Class<?> source, @NonNull Class<?> superType) {
        if (source == superType) {
            return 0;
        }

        int result = 1;

        Class<?> superclass = source;

        do {
            superclass = superclass.getSuperclass();

            if (superclass == superType) {
                return result;
            }
        } while (superclass != null);

        return -1;
    }
}
