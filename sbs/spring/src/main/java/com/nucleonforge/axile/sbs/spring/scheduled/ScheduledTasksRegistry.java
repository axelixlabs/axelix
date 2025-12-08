/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.sbs.spring.scheduled;

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
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.Task;

/**
 * Registry for managing and tracking scheduled tasks within the application.
 * Automatically discovers and registers all scheduled tasks during application startup.
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 */
public class ScheduledTasksRegistry implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasksRegistry.class);

    private final Map<String, ManagedScheduledTask> tasks = new ConcurrentHashMap<>();

    private final ScheduledAnnotationBeanPostProcessor processor;

    private final TaskScheduler scheduler;

    public ScheduledTasksRegistry(ScheduledAnnotationBeanPostProcessor processor, TaskScheduler scheduler) {
        this.processor = processor;
        this.scheduler = scheduler;
    }

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        Set<ScheduledTask> allTasks = processor.getScheduledTasks();
        for (ScheduledTask task : allTasks) {
            register(task);
        }
        log.info("Registered {} managed scheduled tasks", tasks.size());
    }

    private void register(ScheduledTask task) {
        String id = resolveId(task);
        if (tasks.containsKey(id)) {
            return;
        }
        ManagedScheduledTask managed = new ManagedScheduledTask(id, task, scheduler);
        tasks.put(id, managed);
    }

    private String resolveId(ScheduledTask task) {
        Task t = task.getTask();
        Runnable r = t.getRunnable();
        return r.toString();
    }

    public Collection<ManagedScheduledTask> getAll() {
        return tasks.values();
    }

    public Optional<ManagedScheduledTask> find(String id) {
        return Optional.ofNullable(tasks.get(id));
    }
}
