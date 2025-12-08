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
package com.nucleonforge.axile.master.model.software.core;

import com.nucleonforge.axile.master.model.software.LibraryComponent;

/**
 * The Kotlin version used in the app. Discovered by the Kotlin standard library dependency.
 *
 * @author Mikhail Polivakha
 */
public final class KotlinVersion extends LibraryComponent {

    public KotlinVersion() {
        super(
                "kotlin-stdlib",
                "org.jetbrains.kotlin",
                "Kotlin",
                "The version of Kotlin language being used in the application",
                true);
    }
}
