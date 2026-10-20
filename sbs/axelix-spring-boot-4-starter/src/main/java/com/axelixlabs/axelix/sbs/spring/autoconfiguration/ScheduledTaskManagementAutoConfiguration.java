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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskHolder;

import com.axelixlabs.axelix.sbs.spring.autoconfiguration.annotations.AxelixAutoConfiguration;
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
 * @author Dmitry Kiselev
 * @author Ilya Naumov
 * @author Dmitry Mazurov
 * @since 14.10.2025
 */
@AxelixAutoConfiguration
@ConditionalOnAvailableEndpoint(endpoint = AxelixScheduledTasksEndpoint.class)
public class ScheduledTaskManagementAutoConfiguration {

    @Bean
    TaskManagementThreadPools taskManagementThreadPools() {
        return new TaskManagementThreadPools();
    }

    @Bean
    public ScheduledTasksRegistry scheduledTasksRegistry(ObjectProvider<ScheduledTaskHolder> taskHolders) {
        return new ScheduledTasksRegistry(taskHolders.orderedStream().toList());
    }

    @Bean
    ScheduledTaskService scheduledTaskService(
            ScheduledTasksRegistry scheduledTasksRegistry,
            List<TaskRescheduler> taskReschedulers,
            TaskManagementThreadPools taskManagementThreadPools) {
        return new ScheduledTaskService(scheduledTasksRegistry, taskReschedulers, taskManagementThreadPools.executor());
    }

    @Bean
    TaskRescheduler intervalBasedTaskRescheduler(TaskManagementThreadPools taskManagementThreadPools) {
        return new IntervalBasedTaskRescheduler(taskManagementThreadPools.scheduler());
    }

    @Bean
    TaskRescheduler triggerBasedTaskRescheduler(TaskManagementThreadPools taskManagementThreadPools) {
        return new TriggerBasedTaskRescheduler(taskManagementThreadPools.scheduler());
    }

    @Bean
    public AxelixScheduledTasksEndpoint axelixScheduledTasksEndpoint(
            ScheduledTaskService service, ScheduledTasksAssembler scheduledTasksAssembler) {
        return new AxelixScheduledTasksEndpoint(service, scheduledTasksAssembler);
    }

    @Bean
    public ScheduledTasksAssembler scheduledTasksAssembler(ScheduledTasksRegistry scheduledTasksRegistry) {
        return new DefaultScheduledTasksAssembler(scheduledTasksRegistry);
    }

    static final class TaskManagementThreadPools implements AutoCloseable {

        static final String SCHEDULER_NAME = "ax-scheduler-";

        static final String EXECUTOR_NAME = "ax-executor-";

        private final ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        private final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        TaskManagementThreadPools() {
            scheduler.setPoolSize(1);
            scheduler.setThreadNamePrefix(SCHEDULER_NAME);

            executor.setCorePoolSize(1);
            executor.setMaxPoolSize(3);
            executor.setAllowCoreThreadTimeOut(false);
            executor.setPrestartAllCoreThreads(true);
            executor.setAwaitTerminationSeconds(30);
            executor.setWaitForTasksToCompleteOnShutdown(true);
            executor.setThreadNamePrefix(EXECUTOR_NAME);

            scheduler.initialize();
            executor.initialize();
        }

        ThreadPoolTaskScheduler scheduler() {
            return scheduler;
        }

        ThreadPoolTaskExecutor executor() {
            return executor;
        }

        @Override
        public void close() {
            scheduler.destroy();
            executor.destroy();
        }
    }
}
