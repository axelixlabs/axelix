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
package com.nucleonforge.axelix.sbs.autoconfiguration;

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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskHolder;

import com.nucleonforge.axelix.sbs.spring.scheduled.AxelixScheduledTasksEndpoint;
import com.nucleonforge.axelix.sbs.spring.scheduled.DefaultScheduledTasksAssembler;
import com.nucleonforge.axelix.sbs.spring.scheduled.IntervalBasedTaskRescheduler;
import com.nucleonforge.axelix.sbs.spring.scheduled.ScheduledTaskService;
import com.nucleonforge.axelix.sbs.spring.scheduled.ScheduledTasksAssembler;
import com.nucleonforge.axelix.sbs.spring.scheduled.ScheduledTasksRegistry;
import com.nucleonforge.axelix.sbs.spring.scheduled.TaskRescheduler;
import com.nucleonforge.axelix.sbs.spring.scheduled.TriggerBasedTaskRescheduler;

/**
 * Auto-configuration for scheduled task management functionality.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 * @since 14.10.2025
 */
@AutoConfiguration
@ConditionalOnBean(ScheduledAnnotationBeanPostProcessor.class)
public class ScheduledTaskManagementAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ScheduledTasksRegistry scheduledTasksRegistry(ObjectProvider<ScheduledTaskHolder> taskHolders) {
        return new ScheduledTasksRegistry(taskHolders.orderedStream().toList());
    }

    @Bean
    @ConditionalOnMissingBean
    public ScheduledTaskService scheduledTaskService(
            ScheduledTasksRegistry scheduledTasksRegistry,
            List<TaskRescheduler> taskReschedulers,
            ObjectProvider<ThreadPoolTaskExecutor> taskExecutor) {
        return new ScheduledTaskService(
                scheduledTasksRegistry,
                taskReschedulers,
                taskExecutor.getIfAvailable() != null ? taskExecutor.getIfAvailable() : createThreadPoolExecutor());
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
            ScheduledTaskService service, ScheduledTasksAssembler scheduledTasksAssembler) {
        return new AxelixScheduledTasksEndpoint(service, scheduledTasksAssembler);
    }

    @Bean
    @ConditionalOnMissingBean
    public ScheduledTasksAssembler serviceScheduledTasksAssembler(ScheduledTasksRegistry scheduledTasksRegistry) {
        return new DefaultScheduledTasksAssembler(scheduledTasksRegistry);
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

    private static ThreadPoolTaskExecutor createThreadPoolExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(1);
        threadPoolTaskExecutor.setMaxPoolSize(3);
        threadPoolTaskExecutor.setAllowCoreThreadTimeOut(false);
        threadPoolTaskExecutor.setPrestartAllCoreThreads(true);
        return threadPoolTaskExecutor;
    }
}
