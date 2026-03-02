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

    private final @Nullable TaskScheduler taskScheduler;

    public ScheduledTasksRegistry(Collection<ScheduledTaskHolder> scheduledTaskHolders) {
        this(scheduledTaskHolders, null);
    }

    public ScheduledTasksRegistry(
            Collection<ScheduledTaskHolder> scheduledTaskHolders, @Nullable TaskScheduler taskScheduler) {
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
                if (existing == null && taskScheduler != null) {
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
        Runnable originalRunnable = managed.getRunnable();
        TrackingRunnable tracking = new TrackingRunnable(originalRunnable);
        Task originalTask = managed.getTask();
        Task newTask;
        ScheduledFuture<?> newFuture;

        if (originalTask instanceof CronTask cronTask) {
            managed.getScheduledTask().cancel(false);
            Trigger trigger = cronTask.getTrigger();
            if (trigger instanceof CronTrigger cronTrigger) {
                newTask = new CronTask(tracking, cronTrigger);
                newFuture = scheduler.schedule(tracking, cronTrigger);
            } else {
                newTask = new CronTask(tracking, cronTask.getExpression());
                newFuture = scheduler.schedule(tracking, new CronTrigger(cronTask.getExpression()));
            }
        } else if (originalTask instanceof FixedRateTask fixedRateTask) {
            managed.getScheduledTask().cancel(false);
            newTask = new FixedRateTask(
                    tracking, fixedRateTask.getIntervalDuration(), fixedRateTask.getInitialDelayDuration());
            newFuture = scheduler.scheduleAtFixedRate(
                    tracking,
                    Instant.now().plus(fixedRateTask.getInitialDelayDuration()),
                    fixedRateTask.getIntervalDuration());
        } else if (originalTask instanceof FixedDelayTask fixedDelayTask) {
            managed.getScheduledTask().cancel(false);
            newTask = new FixedDelayTask(
                    tracking, fixedDelayTask.getIntervalDuration(), fixedDelayTask.getInitialDelayDuration());
            newFuture = scheduler.scheduleWithFixedDelay(
                    tracking,
                    Instant.now().plus(fixedDelayTask.getInitialDelayDuration()),
                    fixedDelayTask.getIntervalDuration());
        } else if (originalTask instanceof TriggerTask triggerTask) {
            managed.getScheduledTask().cancel(false);
            newTask = new TriggerTask(tracking, triggerTask.getTrigger());
            newFuture = scheduler.schedule(tracking, triggerTask.getTrigger());
        } else {
            return;
        }

        managed.replaceScheduledState(newFuture, newTask);
    }

    private String resolveId(ScheduledTask task) {
        Task t = task.getTask();
        Runnable r = t.getRunnable();
        return r.toString();
    }
}
