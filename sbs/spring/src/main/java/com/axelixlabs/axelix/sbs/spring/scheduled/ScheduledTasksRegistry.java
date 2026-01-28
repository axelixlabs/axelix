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
package com.axelixlabs.axelix.sbs.spring.scheduled;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskHolder;
import org.springframework.scheduling.config.Task;

/**
 * Registry for managing and tracking scheduled tasks within the application.
 * Automatically discovers and registers all scheduled tasks during application startup.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public class ScheduledTasksRegistry implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasksRegistry.class);

    private final Map<String, ManagedScheduledTask> tasks = new ConcurrentHashMap<>();

    private final Collection<ScheduledTaskHolder> scheduledTaskHolders;

    public ScheduledTasksRegistry(Collection<ScheduledTaskHolder> scheduledTaskHolders) {
        this.scheduledTaskHolders = scheduledTaskHolders;
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        for (ScheduledTaskHolder scheduledTaskHolder : scheduledTaskHolders) {
            Set<ScheduledTask> allTasks = scheduledTaskHolder.getScheduledTasks();
            for (ScheduledTask task : allTasks) {
                tasks.computeIfAbsent(resolveId(task), taskId -> new ManagedScheduledTask(taskId, task));
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

    private String resolveId(ScheduledTask task) {
        Task t = task.getTask();
        Runnable r = t.getRunnable();
        return r.toString();
    }
}
