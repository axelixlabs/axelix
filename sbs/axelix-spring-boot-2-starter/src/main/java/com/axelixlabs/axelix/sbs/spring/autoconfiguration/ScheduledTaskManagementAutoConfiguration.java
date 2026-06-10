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
import java.util.stream.Collectors;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.ScheduledTaskHolder;

import com.axelixlabs.axelix.sbs.spring.core.scheduled.AxelixScheduledTasksEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.DefaultScheduledTasksAssembler;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.IntervalBasedTaskRescheduler;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTaskService;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTasksAssembler;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTasksRegistry;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.TaskRescheduler;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.TriggerBasedTaskRescheduler;

/**
 * Auto-configuration for scheduled task management functionality.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 * @since 14.10.2025
 */
@AutoConfiguration
@ConditionalOnAvailableEndpoint(endpoint = AxelixScheduledTasksEndpoint.class)
public class ScheduledTaskManagementAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ScheduledTasksRegistry scheduledTasksRegistry(ObjectProvider<ScheduledTaskHolder> taskHolders) {
        return new ScheduledTasksRegistry(taskHolders.orderedStream().collect(Collectors.toList()));
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
    @ConditionalOnBean(TaskScheduler.class)
    public TaskRescheduler intervalBasedTaskRescheduler(TaskScheduler scheduler) {
        return new IntervalBasedTaskRescheduler(scheduler);
    }

    @Bean
    @ConditionalOnBean(TaskScheduler.class)
    public TaskRescheduler triggerBasedTaskRescheduler(TaskScheduler scheduler) {
        return new TriggerBasedTaskRescheduler(scheduler);
    }

    @Bean
    @ConditionalOnMissingBean
    public AxelixScheduledTasksEndpoint axelixScheduledTasksEndpoint(
            ScheduledTaskService service, ScheduledTasksAssembler scheduledTasksAssembler) {
        return new AxelixScheduledTasksEndpoint(service, scheduledTasksAssembler);
    }

    @Bean
    @ConditionalOnMissingBean
    public ScheduledTasksAssembler scheduledTasksAssembler(ScheduledTasksRegistry scheduledTasksRegistry) {
        return new DefaultScheduledTasksAssembler(scheduledTasksRegistry);
    }

    private static ThreadPoolTaskExecutor createThreadPoolExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(1);
        threadPoolTaskExecutor.setMaxPoolSize(3);
        threadPoolTaskExecutor.setAllowCoreThreadTimeOut(false);
        threadPoolTaskExecutor.setPrestartAllCoreThreads(true);
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }
}
