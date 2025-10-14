package com.nucleonforge.axile.master.api.request;

/**
 * Request to enable eecution of a scheduled task.
 *
 * @param targetScheduledTask   The target scheduled task to be enabled.
 * @param force                 The value {@code true} forces an immediate change of the task state:
 *                              it will forcibly enable the task if inactive, or interrupt it if currently running,
 *                              regardless of the schedule settings. {@code false} means the task will follow the configured schedule.
 *
 * @author Sergey Cherkasov
 */
public record ScheduledTaskToggleRequest(String targetScheduledTask, boolean force) {}
