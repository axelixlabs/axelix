package com.nucleonforge.axile.sbs.spring.scheduled;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.EndpointWebExtension;
import org.springframework.boot.actuate.scheduling.ScheduledTasksEndpoint;

/**
 * Web extension for the {@link ScheduledTasksEndpoint} that enhances the standard
 * scheduled tasks information with additional management capabilities.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 */
@EndpointWebExtension(endpoint = ScheduledTasksEndpoint.class)
public class ScheduledTasksEndpointExtension {

    private final ScheduledTasksEndpoint delegate;

    private final ScheduledTasksRegistry registry;

    public ScheduledTasksEndpointExtension(ScheduledTasksEndpoint delegate, ScheduledTasksRegistry registry) {
        this.delegate = delegate;
        this.registry = registry;
    }

    @ReadOperation
    public ExtendedScheduledTasksDescriptor scheduledTasks() {
        ScheduledTasksEndpoint.ScheduledTasksDescriptor scheduledTasksDescriptor = delegate.scheduledTasks();

        List<ExtendedTaskDescriptor> cronTasks = enrich(scheduledTasksDescriptor.getCron());
        List<ExtendedTaskDescriptor> fixedDelayTasks = enrich(scheduledTasksDescriptor.getFixedDelay());
        List<ExtendedTaskDescriptor> fixedRateTasks = enrich(scheduledTasksDescriptor.getFixedRate());
        List<ExtendedTaskDescriptor> customTasks = enrich(scheduledTasksDescriptor.getCustom());

        return new ExtendedScheduledTasksDescriptor(cronTasks, fixedDelayTasks, fixedRateTasks, customTasks);
    }

    private List<ExtendedTaskDescriptor> enrich(List<? extends ScheduledTasksEndpoint.TaskDescriptor> tasks) {
        return tasks.stream()
                .map(td -> new ExtendedTaskDescriptor(td, resolveTaskEnabledStatus(td)))
                .toList();
    }

    private boolean resolveTaskEnabledStatus(ScheduledTasksEndpoint.TaskDescriptor taskDescriptor) {
        String target = taskDescriptor.getRunnable().getTarget();

        // TODO:
        //  1. how is that possible that future will be null?
        //  2. is that correct that we're returning tru in case the task is not found? I guess no.
        return registry.find(target)
                .map(task -> {
                    ScheduledFuture<?> future = task.getFuture();
                    return future == null || !future.isCancelled();
                })
                .orElse(true);
    }

    public record ExtendedScheduledTasksDescriptor(
            List<ExtendedTaskDescriptor> cron,
            List<ExtendedTaskDescriptor> fixedDelay,
            List<ExtendedTaskDescriptor> fixedRate,
            List<ExtendedTaskDescriptor> custom) {}

    public record ExtendedTaskDescriptor(ScheduledTasksEndpoint.TaskDescriptor delegate, boolean enabled) {}
}
