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

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.master.api.request.deserilize.StateExportComponentDeserializer;

/**
 * Request for export for of the state of the application.
 *
 * @param components List of components to export with their settings.
 * @author Mikhail Polivakha
 */
public record StateExportRequest(
        @NonNull @JsonDeserialize(using = StateExportComponentDeserializer.class)
                List<StateComponentSettings> components) {

    @Override
    public List<StateComponentSettings> components() {
        return components;
    }
}
