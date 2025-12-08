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
package com.nucleonforge.axile.common.utils;

import java.util.Set;

/**
 * Utils for working with Java Collection Framework.
 *
 * @author Mikhail Polivakha
 */
public class CollectionUtils {

    public static <T> Set<T> defaultIfEmpty(Set<T> source, T defaultValue) {
        if (source == null || source.isEmpty()) {
            return Set.of(defaultValue);
        } else {
            return source;
        }
    }
}
