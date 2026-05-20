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
package com.axelixlabs.axelix.sbs.spring.core.scheduled;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskHolder;
import org.springframework.scheduling.config.Task;
import org.springframework.scheduling.config.TriggerTask;
import org.springframework.scheduling.support.CronTrigger;

/**
 * Registry for managing and tracking scheduled tasks within the application.
 * Automatically discovers and registers all scheduled tasks during application startup.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Aleksei Ermakov
 */
public class ScheduledTasksRegistry implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasksRegistry.class);

    private final Map<String, ManagedScheduledTask> tasks = new ConcurrentHashMap<>();

    private final Collection<ScheduledTaskHolder> scheduledTaskHolders;

    private final TaskScheduler taskScheduler;

    public ScheduledTasksRegistry(Collection<ScheduledTaskHolder> scheduledTaskHolders, TaskScheduler taskScheduler) {
        this.scheduledTaskHolders = scheduledTaskHolders;
        this.taskScheduler = taskScheduler;
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        for (ScheduledTaskHolder scheduledTaskHolder : scheduledTaskHolders) {
            Set<ScheduledTask> allTasks = scheduledTaskHolder.getScheduledTasks();
            for (ScheduledTask task : allTasks) {
                String taskId = resolveId(task);
                ManagedScheduledTask managed = new ManagedScheduledTask(taskId, task);
                ManagedScheduledTask existing = tasks.putIfAbsent(taskId, managed);
                if (existing == null) {
                    wrapAndReschedule(managed, taskScheduler);
                }
            }
        }
        log.info("Registered {} managed scheduled tasks", tasks.size());
    }

    public Collection<ManagedScheduledTask> getAll() {
        return tasks.values();
    }

    public Optional<ManagedScheduledTask> find(String id) {
        return Optional.ofNullable(tasks.get(id));
    }

    public ManagedScheduledTask findRequired(String id) {
        return Optional.ofNullable(tasks.get(id))
                .orElseThrow(() -> new ScheduledTaskNotFoundException("Task not found: " + id));
    }

    private void wrapAndReschedule(ManagedScheduledTask managed, TaskScheduler scheduler) {
        TrackingRunnable tracking = new TrackingRunnable(managed.getRunnable());
        RescheduledState rescheduledState = reschedule(managed.getTask(), tracking, scheduler);
        if (rescheduledState == null) {
            return;
        }

        managed.getScheduledTask().cancel(false);
        managed.replaceScheduledState(rescheduledState.getFuture(), rescheduledState.getTask());
    }

    private @Nullable RescheduledState reschedule(
            Task originalTask, TrackingRunnable tracking, TaskScheduler scheduler) {
        if (originalTask instanceof CronTask) {
            return rescheduleCron((CronTask) originalTask, tracking, scheduler);
        }
        if (originalTask instanceof FixedRateTask) {
            return rescheduleFixedRate((FixedRateTask) originalTask, tracking, scheduler);
        }
        if (originalTask instanceof FixedDelayTask) {
            return rescheduleFixedDelay((FixedDelayTask) originalTask, tracking, scheduler);
        }
        if (originalTask instanceof TriggerTask) {
            return rescheduleTrigger((TriggerTask) originalTask, tracking, scheduler);
        }
        return null;
    }

    private RescheduledState rescheduleCron(CronTask originalTask, TrackingRunnable tracking, TaskScheduler scheduler) {
        Trigger trigger = originalTask.getTrigger();
        if (trigger instanceof CronTrigger cronTrigger) {
            Task newTask = new CronTask(tracking, cronTrigger);
            ScheduledFuture<?> newFuture = scheduler.schedule(tracking, cronTrigger);
            return new RescheduledState(newTask, newFuture);
        }

        Task newTask = new CronTask(tracking, originalTask.getExpression());
        ScheduledFuture<?> newFuture = scheduler.schedule(tracking, new CronTrigger(originalTask.getExpression()));
        return new RescheduledState(newTask, newFuture);
    }

    private RescheduledState rescheduleFixedRate(
            FixedRateTask originalTask, TrackingRunnable tracking, TaskScheduler scheduler) {
        Task newTask =
                new FixedRateTask(tracking, originalTask.getIntervalDuration(), originalTask.getInitialDelayDuration());
        ScheduledFuture<?> newFuture = scheduler.scheduleAtFixedRate(
                tracking,
                Instant.now().plus(originalTask.getInitialDelayDuration()),
                originalTask.getIntervalDuration());
        return new RescheduledState(newTask, newFuture);
    }

    private RescheduledState rescheduleFixedDelay(
            FixedDelayTask originalTask, TrackingRunnable tracking, TaskScheduler scheduler) {
        Task newTask = new FixedDelayTask(
                tracking, originalTask.getIntervalDuration(), originalTask.getInitialDelayDuration());
        ScheduledFuture<?> newFuture = scheduler.scheduleWithFixedDelay(
                tracking,
                Instant.now().plus(originalTask.getInitialDelayDuration()),
                originalTask.getIntervalDuration());
        return new RescheduledState(newTask, newFuture);
    }

    private RescheduledState rescheduleTrigger(
            TriggerTask originalTask, TrackingRunnable tracking, TaskScheduler scheduler) {
        Task newTask = new TriggerTask(tracking, originalTask.getTrigger());
        ScheduledFuture<?> newFuture = scheduler.schedule(tracking, originalTask.getTrigger());
        return new RescheduledState(newTask, newFuture);
    }

    private String resolveId(ScheduledTask task) {
        Task t = task.getTask();
        Runnable r = t.getRunnable();
        return r.toString();
    }
}
