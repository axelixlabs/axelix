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
