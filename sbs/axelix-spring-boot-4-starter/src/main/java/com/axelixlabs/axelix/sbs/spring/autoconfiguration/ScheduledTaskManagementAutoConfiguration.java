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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.config.ScheduledTaskHolder;

import com.axelixlabs.axelix.sbs.spring.autoconfiguration.annotations.AxelixAutoConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ApplicationTaskSchedulerProvider;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.AxelixScheduledAnnotationBeanPostProcessor;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.AxelixScheduledAnnotationProcessorRegistrar;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.AxelixScheduledTasksEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.DefaultScheduledTasksAssembler;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.IntervalBasedTaskRescheduler;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTaskExecutionHistory;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTaskService;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTasksAssembler;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTasksRegistry;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.TaskRescheduler;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.TaskSchedulerProvider;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.TriggerBasedTaskRescheduler;

/**
 * Auto-configuration for scheduled task management functionality.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 * @author Dmitry Kiselev
 * @author Ilya Naumov
 * @author Dmitry Mazurov
 * @since 14.10.2025
 */
@AxelixAutoConfiguration
@ConditionalOnAvailableEndpoint(endpoint = AxelixScheduledTasksEndpoint.class)
public class ScheduledTaskManagementAutoConfiguration {

    // Static, since it is a BeanDefinitionRegistryPostProcessor.
    @Bean
    public static AxelixScheduledAnnotationProcessorRegistrar axelixScheduledAnnotationProcessorRegistrar() {
        return new AxelixScheduledAnnotationProcessorRegistrar();
    }

    // The declared type must not be a SchedulingConfigurer: Spring Boot looks at it only, and in the older versions
    // it does not create its own 'taskScheduler' when there is a SchedulingConfigurer bean. Spring itself still
    // finds the configurer by the actual type of the instance, so the bean must be created before the context
    // refresh, i.e. it must not be lazy.
    @Bean
    @Lazy(false)
    public TaskSchedulerProvider taskSchedulerProvider() {
        return new ApplicationTaskSchedulerProvider();
    }

    @Bean
    public ScheduledTasksRegistry scheduledTasksRegistry(ObjectProvider<ScheduledTaskHolder> taskHolders) {
        return new ScheduledTasksRegistry(taskHolders.orderedStream().toList());
    }

    @Bean
    public ScheduledTaskService scheduledTaskService(
            ScheduledTasksRegistry scheduledTasksRegistry,
            List<TaskRescheduler> taskReschedulers,
            TaskSchedulerProvider taskSchedulerProvider,
            ObjectProvider<AxelixScheduledAnnotationBeanPostProcessor> scheduledAnnotationProcessor) {
        AxelixScheduledAnnotationBeanPostProcessor processor = scheduledAnnotationProcessor.getIfAvailable();
        return new ScheduledTaskService(
                scheduledTasksRegistry,
                taskReschedulers,
                taskSchedulerProvider,
                processor != null ? processor.getRegistrar() : null);
    }

    @Bean
    public TaskRescheduler intervalBasedTaskRescheduler(TaskSchedulerProvider taskSchedulerProvider) {
        return new IntervalBasedTaskRescheduler(taskSchedulerProvider);
    }

    @Bean
    public TaskRescheduler triggerBasedTaskRescheduler(TaskSchedulerProvider taskSchedulerProvider) {
        return new TriggerBasedTaskRescheduler(taskSchedulerProvider);
    }

    @Bean
    public AxelixScheduledTasksEndpoint axelixScheduledTasksEndpoint(
            ScheduledTaskService service, ScheduledTasksAssembler scheduledTasksAssembler) {
        return new AxelixScheduledTasksEndpoint(service, scheduledTasksAssembler);
    }

    @Bean
    public ScheduledTaskExecutionHistory scheduledTaskExecutionHistory() {
        return new ScheduledTaskExecutionHistory();
    }

    @Bean
    public ScheduledTasksAssembler scheduledTasksAssembler(ScheduledTasksRegistry scheduledTasksRegistry) {
        return new DefaultScheduledTasksAssembler(scheduledTasksRegistry);
    }
}
