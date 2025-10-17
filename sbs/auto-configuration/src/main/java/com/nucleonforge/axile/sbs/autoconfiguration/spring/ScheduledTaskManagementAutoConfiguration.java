package com.nucleonforge.axile.sbs.autoconfiguration.spring;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.scheduling.ScheduledTasksEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

import com.nucleonforge.axile.sbs.spring.scheduled.ScheduledTaskManagementEndpoint;
import com.nucleonforge.axile.sbs.spring.scheduled.ScheduledTaskService;
import com.nucleonforge.axile.sbs.spring.scheduled.ScheduledTasksEndpointExtension;
import com.nucleonforge.axile.sbs.spring.scheduled.ScheduledTasksRegistry;

/**
 * Auto-configuration for scheduled task management functionality.
 *
 * @author Nikita Kirillov
 * @since 14.10.2025
 */
@AutoConfiguration
@ConditionalOnAvailableEndpoint(endpoint = ScheduledTasksEndpoint.class)
@ConditionalOnBean(ScheduledAnnotationBeanPostProcessor.class)
public class ScheduledTaskManagementAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ScheduledTasksRegistry scheduledTasksRegistry(
            ScheduledAnnotationBeanPostProcessor processor, TaskScheduler scheduler) {
        return new ScheduledTasksRegistry(processor, scheduler);
    }

    @Bean
    @ConditionalOnMissingBean
    public ScheduledTaskService scheduledTaskService(ScheduledTasksRegistry scheduledTasksRegistry) {
        return new ScheduledTaskService(scheduledTasksRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public ScheduledTasksEndpointExtension scheduledTasksEndpointExtension(
            ScheduledTasksEndpoint delegate, ScheduledTasksRegistry scheduledTasksRegistry) {
        return new ScheduledTasksEndpointExtension(delegate, scheduledTasksRegistry);
    }

    @Bean
    @ConditionalOnMissingBean
    public ScheduledTaskManagementEndpoint scheduledTaskManagementEndpoint(ScheduledTaskService scheduledTaskService) {
        return new ScheduledTaskManagementEndpoint(scheduledTaskService);
    }
}
