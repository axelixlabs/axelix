package com.nucleonforge.axile.master.service.state.export;

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
public class ScheduledTasksDataCollector implements StateDataCollector {

    private final ScheduledTasksApi scheduledTasksApi;

    public ScheduledTasksDataCollector(ScheduledTasksApi scheduledTasksApi) {
        this.scheduledTasksApi = scheduledTasksApi;
    }

    @Override
    public String getName() {
        return "scheduled";
    }

    @Override
    public Object collectData(String instanceId) {
        return scheduledTasksApi.getAllScheduledTasks(instanceId);
    }
}
