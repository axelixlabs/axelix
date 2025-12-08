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
package com.nucleonforge.axile.master.api.request.state;

import org.jspecify.annotations.Nullable;

/**
 * The possible exportable state component.
 *
 * @see com.nucleonforge.axile.master.api.StateExportApi
 * @author Mikhail Polivakha
 */
public enum StateExportComponent {
    HEAP_DUMP,
    THREAD_DUMP,
    BEANS,
    CACHES,
    CONDITIONS,
    CONFIG_PROPS,
    ENV,
    LOG_FILE,
    SCHEDULED_TASKS;

    @Nullable
    public static StateExportComponent valueOfIgnoreCase(String component) {
        for (StateExportComponent value : values()) {
            if (value.name().equalsIgnoreCase(component)) {
                return value;
            }
        }

        return null;
    }
}
