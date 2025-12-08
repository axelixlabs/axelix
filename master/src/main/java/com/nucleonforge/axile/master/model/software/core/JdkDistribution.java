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
package com.nucleonforge.axile.master.model.software.core;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.master.model.software.SoftwareComponent;

public final class JdkDistribution implements SoftwareComponent {

    @Override
    public @NonNull String getName() {
        return "JDK Distribution";
    }

    @Override
    public String getDescription() {
        return "The distribution of JDK being used in the application";
    }

    @Override
    public boolean isCore() {
        return true;
    }
}
