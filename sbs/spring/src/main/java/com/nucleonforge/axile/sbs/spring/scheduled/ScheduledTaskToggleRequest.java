package com.nucleonforge.axile.sbs.spring.scheduled;

/**
 * Represents a request to toggle (enable/disable) a scheduled task.
 *
 * @param targetScheduledTask the identifier of the scheduled task to toggle. Must not be {@code null}.
 * @param force  if {@code true} and enabling - the task will start immediately regardless of its delay;
 *               if {@code true} and disabling - the task will be interrupted even if currently running.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 */
public record ScheduledTaskToggleRequest(String targetScheduledTask, boolean force) {}
