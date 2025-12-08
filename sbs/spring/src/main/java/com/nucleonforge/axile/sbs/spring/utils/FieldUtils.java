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
package com.nucleonforge.axile.sbs.spring.utils;

import java.lang.reflect.Field;

import org.springframework.util.ReflectionUtils;

/**
 * Utils for working with {@link Field} descriptors.
 *
 * @author Mikhail Polivakha
 */
public class FieldUtils {

    @SuppressWarnings({"unchecked", "NullAway"})
    public static <T> T getField(String name, Object onObject) {
        try {
            Field field = ReflectionUtils.findField(onObject.getClass(), name);
            if (field == null) {
                throw new NoSuchFieldException(name);
            }
            ReflectionUtils.makeAccessible(field);
            return (T) ReflectionUtils.getField(field, onObject);
        } catch (ClassCastException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
