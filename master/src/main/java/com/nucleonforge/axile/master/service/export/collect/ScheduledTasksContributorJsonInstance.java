package com.nucleonforge.axile.master.service.export.collect;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.ScheduledTasksApi;

/**
 * Collects Scheduled Tasks information for application state export.
 *
 * @see ScheduledTasksApi
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
@Component
public class ScheduledTasksContributorJsonInstance extends AbstractJsonInstanceStateCollector {

    private final ScheduledTasksApi scheduledTasksApi;

    public ScheduledTasksContributorJsonInstance(ScheduledTasksApi scheduledTasksApi) {
        this.scheduledTasksApi = scheduledTasksApi;
    }

    @Override
    public String getName() {
        return "scheduled";
    }

    @Override
    protected Object collectInternal(String instanceId) {
        return scheduledTasksApi.getAllScheduledTasks(instanceId);
    }
}
