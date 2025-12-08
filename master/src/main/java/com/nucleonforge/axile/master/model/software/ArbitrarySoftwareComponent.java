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
package com.nucleonforge.axile.master.model.software;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Any arbitrary {@link SoftwareComponent versionable component} that is used in the app, like
 * any specific library e.g. jackson, vavr, rabbitmq-client etc.
 *
 * @author Mikhail Polivakha
 */
public record ArbitrarySoftwareComponent(String name, String description, boolean isCore) implements SoftwareComponent {

    @Override
    public @NonNull String getName() {
        return name;
    }

    @Override
    public @Nullable String getDescription() {
        return description;
    }

    @Override
    public boolean isCore() {
        return isCore;
    }
}
