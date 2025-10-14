package com.nucleonforge.axile.spring.scheduled;

import java.time.Instant;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.Task;

/**
 * Service for managing scheduled tasks with enable/disable functionality.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 */
public class ScheduledTaskService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskService.class);

    private final ScheduledTasksRegistry registry;

    public ScheduledTaskService(ScheduledTasksRegistry registry) {
        this.registry = registry;
    }

    public void enableTask(String target, boolean force) {
        try {
            ManagedScheduledTask task = registry.find(target)
                    .orElseThrow(() -> new ScheduledTaskNotFoundException("Task not found: " + target));

            if (!task.isEnabled()) {
                rescheduleTask(task, force);
                task.setEnabled(true);
                log.info("Enabled scheduled task: {}", target);
            } else if (force) {
                rescheduleTask(task, true);
                log.info("Forcefully rescheduled enabled task: {}", target);
            } else {
                log.info("Task already enabled, no action taken: {}", target);
            }
        } catch (ScheduledTaskNotFoundException e) {
            log.info("Failed to enable task: {}", target, e);
            throw e;
        }
    }

    public void disableTask(String target, boolean force) {
        try {
            ManagedScheduledTask task = registry.find(target)
                    .orElseThrow(() -> new ScheduledTaskNotFoundException("Task not found: " + target));

            cancelTask(task, force);
            task.setEnabled(false);
            log.info("Disabled scheduled task: {}(force: {})", target, force);
        } catch (ScheduledTaskNotFoundException e) {
            log.info("Failed to disable task: {}", target, e);
            throw e;
        }
    }

    private void rescheduleTask(ManagedScheduledTask managedTask, boolean runImmediately) {
        cancelTask(managedTask, runImmediately);

        Task task = managedTask.getScheduledTask().getTask();
        TaskScheduler taskScheduler = managedTask.getTaskScheduler();

        ScheduledFuture<?> newFuture;
        if (managedTask.getTrigger() != null) {
            if (runImmediately) {
                taskScheduler.schedule(managedTask.getRunnable(), Instant.now());
            }
            newFuture = taskScheduler.schedule(managedTask.getRunnable(), managedTask.getTrigger());
        } else if (task instanceof FixedRateTask frt) {
            newFuture = taskScheduler.scheduleAtFixedRate(
                    managedTask.getRunnable(),
                    runImmediately ? Instant.now() : Instant.now().plus(frt.getInitialDelayDuration()),
                    frt.getIntervalDuration());
        } else if (task instanceof FixedDelayTask fdt) {
            newFuture = taskScheduler.scheduleWithFixedDelay(
                    managedTask.getRunnable(),
                    runImmediately ? Instant.now() : Instant.now().plus(fdt.getInitialDelayDuration()),
                    fdt.getIntervalDuration());
        } else {
            String errorMessage = String.format(
                    "Unsupported task type: %s for task: %s", task.getClass().getName(), managedTask.getId());
            log.info(errorMessage);
            throw new UnsupportedTaskTypeException(errorMessage);
        }

        if (newFuture != null) {
            managedTask.setFuture(newFuture);
        }
        log.debug("Rescheduled task: {} (runImmediately: {})", managedTask.getId(), runImmediately);
    }

    private void cancelTask(ManagedScheduledTask managedTask, boolean mayInterruptIfRunning) {
        ScheduledFuture<?> future = managedTask.getFuture();
        if (future != null) {
            boolean cancelled = future.cancel(mayInterruptIfRunning);
            log.debug(
                    "Cancelled task future: {} (mayInterrupt: {}, success: {})",
                    managedTask.getId(),
                    mayInterruptIfRunning,
                    cancelled);
        } else {
            log.debug("No future to cancel for task: {}", managedTask.getId());
        }
    }
}
