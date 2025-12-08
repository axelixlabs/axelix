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
package com.nucleonforge.axile.master.service.convert.request;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.request.state.StateExportRequest;
import com.nucleonforge.axile.master.service.convert.response.Converter;
import com.nucleonforge.axile.master.service.export.StateComponentSettings;
import com.nucleonforge.axile.master.service.export.StateExport;
import com.nucleonforge.axile.master.service.export.settings.BeansStateComponentSettings;
import com.nucleonforge.axile.master.service.export.settings.CachesStateComponentSettings;
import com.nucleonforge.axile.master.service.export.settings.ConditionsStateComponentSettings;
import com.nucleonforge.axile.master.service.export.settings.ConfigPropsStateComponentSettings;
import com.nucleonforge.axile.master.service.export.settings.EnvStateComponentSettings;
import com.nucleonforge.axile.master.service.export.settings.HeapDumpStateComponentSettings;
import com.nucleonforge.axile.master.service.export.settings.LogFileStateComponentSettings;
import com.nucleonforge.axile.master.service.export.settings.ScheduledTasksStateComponentSettings;
import com.nucleonforge.axile.master.service.export.settings.ThreadDumpStateComponentSettings;

/**
 * Converter from {@link StateExportRequest} to {@link StateExport}.
 *
 * @author Mikhail Polivakha
 */
@Component
public class StateExportRequestConverter implements Converter<StateExportRequest, StateExport> {

    @Override
    public @NonNull StateExport convertInternal(@NonNull StateExportRequest source) {
        return new StateExport(source.components().stream().map(this::map).toList());
    }

    // cyclomatic complexity skyrockets because of the switch
    @SuppressWarnings("PMD.CyclomaticComplexity")
    private StateComponentSettings map(com.nucleonforge.axile.master.api.request.state.StateComponentSettings it) {
        return switch (it.getComponent()) {
            case HEAP_DUMP ->
                new HeapDumpStateComponentSettings(
                        ((com.nucleonforge.axile.master.api.request.state.HeapDumpStateComponentSettings) it)
                                .sanitized());
            case THREAD_DUMP -> new ThreadDumpStateComponentSettings();
            case BEANS -> new BeansStateComponentSettings();
            case CACHES -> new CachesStateComponentSettings();
            case CONDITIONS -> new ConditionsStateComponentSettings();
            case CONFIG_PROPS -> new ConfigPropsStateComponentSettings();
            case ENV -> new EnvStateComponentSettings();
            case LOG_FILE -> new LogFileStateComponentSettings();
            case SCHEDULED_TASKS -> new ScheduledTasksStateComponentSettings();
        };
    }
}
