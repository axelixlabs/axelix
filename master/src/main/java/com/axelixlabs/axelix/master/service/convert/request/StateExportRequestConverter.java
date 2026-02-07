/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.master.service.convert.request;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.master.api.external.request.state.StateComponentSettings;
import com.axelixlabs.axelix.master.api.external.request.state.StateExportRequest;
import com.axelixlabs.axelix.master.service.convert.response.Converter;
import com.axelixlabs.axelix.master.service.export.StateExport;
import com.axelixlabs.axelix.master.service.export.settings.BeansStateComponentSettings;
import com.axelixlabs.axelix.master.service.export.settings.CachesStateComponentSettings;
import com.axelixlabs.axelix.master.service.export.settings.ConditionsStateComponentSettings;
import com.axelixlabs.axelix.master.service.export.settings.ConfigPropsStateComponentSettings;
import com.axelixlabs.axelix.master.service.export.settings.EnvStateComponentSettings;
import com.axelixlabs.axelix.master.service.export.settings.GcLogFileStateComponentSettings;
import com.axelixlabs.axelix.master.service.export.settings.HeapDumpStateComponentSettings;
import com.axelixlabs.axelix.master.service.export.settings.ScheduledTasksStateComponentSettings;
import com.axelixlabs.axelix.master.service.export.settings.ThreadDumpStateComponentSettings;

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
    private com.axelixlabs.axelix.master.service.export.StateComponentSettings map(StateComponentSettings it) {
        return switch (it.getComponent()) {
            case HEAP_DUMP ->
                new HeapDumpStateComponentSettings(
                        ((com.axelixlabs.axelix.master.api.external.request.state.HeapDumpStateComponentSettings) it)
                                .sanitized());
            case THREAD_DUMP -> new ThreadDumpStateComponentSettings();
            case BEANS -> new BeansStateComponentSettings();
            case CACHES -> new CachesStateComponentSettings();
            case CONDITIONS -> new ConditionsStateComponentSettings();
            case CONFIG_PROPS -> new ConfigPropsStateComponentSettings();
            case ENV -> new EnvStateComponentSettings();
            case GC_LOG_FILE -> new GcLogFileStateComponentSettings();
            case SCHEDULED_TASKS -> new ScheduledTasksStateComponentSettings();
        };
    }
}
