package com.nucleonforge.axile.master.service.export.collect;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.ScheduledTasksApi;
import com.nucleonforge.axile.master.service.export.StateComponent;
import com.nucleonforge.axile.master.service.export.settings.ScheduledTasksStateComponentSettings;

/**
 * Collects Scheduled Tasks information for application state export.
 *
 * @see ScheduledTasksApi
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
@Component
public class ScheduledTasksContributorJsonInstance
        extends AbstractJsonInstanceStateCollector<ScheduledTasksStateComponentSettings> {

    private final ScheduledTasksApi scheduledTasksApi;

    public ScheduledTasksContributorJsonInstance(ScheduledTasksApi scheduledTasksApi) {
        this.scheduledTasksApi = scheduledTasksApi;
    }

    @Override
    public StateComponent responsibleFor() {
        return StateComponent.SCHEDULED_TASKS;
    }

    @Override
    protected Object collectInternal(String instanceId, ScheduledTasksStateComponentSettings settings) {
        return scheduledTasksApi.getAllScheduledTasks(instanceId);
    }
}
