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
package com.nucleonforge.axile.master.api.request.state;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * {@link StateComponentSettings} for heap dumps.
 *
 * @param sanitized Whether to provide sanitized heap.
 * @author Mikhail Polivakha
 */
public record HeapDumpStateComponentSettings(boolean sanitized) implements StateComponentSettings {

    @JsonGetter(COMPONENT)
    @Override
    public StateExportComponent getComponent() {
        return StateExportComponent.HEAP_DUMP;
    }
}
