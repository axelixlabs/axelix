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
package com.nucleonforge.axelix.sbs.autoconfiguration.spring;

import java.util.List;

import org.jspecify.annotations.NonNull;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.ScheduledTaskHolder;

import com.nucleonforge.axelix.sbs.spring.scheduled.AxelixScheduledTasksEndpoint;
import com.nucleonforge.axelix.sbs.spring.scheduled.IntervalBasedTaskRescheduler;
import com.nucleonforge.axelix.sbs.spring.scheduled.ScheduledTaskManagementEndpoint;
import com.nucleonforge.axelix.sbs.spring.scheduled.ScheduledTaskService;
import com.nucleonforge.axelix.sbs.spring.scheduled.ScheduledTasksRegistry;
import com.nucleonforge.axelix.sbs.spring.scheduled.TaskRescheduler;
import com.nucleonforge.axelix.sbs.spring.scheduled.TriggerBasedTaskRescheduler;

/**
 * Auto-configuration for scheduled task management functionality.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @since 14.10.2025
 */
@AutoConfiguration
@ConditionalOnBean(ScheduledAnnotationBeanPostProcessor.class)
public class ScheduledTaskManagementAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ScheduledTasksRegistry scheduledTasksRegistry(ScheduledAnnotationBeanPostProcessor processor) {
        return new ScheduledTasksRegistry(processor);
    }

    @Bean
    @ConditionalOnMissingBean
    public ScheduledTaskService scheduledTaskService(
            ScheduledTasksRegistry scheduledTasksRegistry, List<TaskRescheduler> taskReschedulers) {
        return new ScheduledTaskService(scheduledTasksRegistry, taskReschedulers);
    }

    @Bean
    public TaskRescheduler intervalBasedTaskRescheduler(ObjectProvider<TaskScheduler> scheduler) {
        TaskScheduler taskScheduler = requireTaskScheduler(scheduler);

        return new IntervalBasedTaskRescheduler(taskScheduler);
    }

    @Bean
    public TaskRescheduler triggerBasedTaskRescheduler(ObjectProvider<TaskScheduler> scheduler) {
        TaskScheduler taskScheduler = requireTaskScheduler(scheduler);

        return new TriggerBasedTaskRescheduler(taskScheduler);
    }

    @Bean
    @ConditionalOnMissingBean
    public AxelixScheduledTasksEndpoint scheduledTasksEndpointExtension(
            ObjectProvider<ScheduledTaskHolder> taskHolders, ScheduledTasksRegistry scheduledTasksRegistry) {
        return new AxelixScheduledTasksEndpoint(taskHolders.orderedStream().toList(), scheduledTasksRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public ScheduledTaskManagementEndpoint scheduledTaskManagementEndpoint(ScheduledTaskService scheduledTaskService) {
        return new ScheduledTaskManagementEndpoint(scheduledTaskService);
    }

    @NonNull
    private static TaskScheduler requireTaskScheduler(ObjectProvider<TaskScheduler> scheduler) {
        TaskScheduler taskScheduler = scheduler.getIfAvailable();

        if (taskScheduler == null) {
            throw new NoSuchBeanDefinitionException(
                    "For @Scheduled-related abilities to work, Axelix requires a bean of type %s that cannot be found"
                            .formatted(TaskScheduler.class.getName()));
        }

        return taskScheduler;
    }
}
