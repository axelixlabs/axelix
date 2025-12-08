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
package com.nucleonforge.axile.sbs.spring.metrics.transform.units;

import java.util.Set;

import org.jspecify.annotations.Nullable;

/**
 * Megabytes {@link MemoryBaseUnit}.
 *
 * @author Mikhail Polivakha
 */
public class MegabytesMemoryBaseUnit extends MemoryBaseUnit {

    public static final MegabytesMemoryBaseUnit INSTANCE = new MegabytesMemoryBaseUnit(Set.of("megabytes"), "MB");

    public MegabytesMemoryBaseUnit(Set<String> aliases, String displayName) {
        super(aliases, displayName);
    }

    @Nullable
    @Override
    public MemoryBaseUnit next() {
        return null;
    }
}
