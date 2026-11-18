package com.axelixlabs.playground.greeting.quartz;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz job and trigger for checking the scheduler metadata reported to Axelix.
 */
@Configuration(proxyBeanMethods = false)
public class QuartzTestConfig {

    @Bean
    public JobDetail loggingJobDetail() {
        return JobBuilder.newJob(LoggingQuartzJob.class)
                .withIdentity("playground-job", "playground")
                .withDescription("Periodic job for Axelix Quartz metadata testing")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger loggingJobTrigger(JobDetail loggingJobDetail) {
        return TriggerBuilder.newTrigger()
                .withIdentity("playground-trigger", "playground")
                .forJob(loggingJobDetail)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(30)
                        .repeatForever())
                .build();
    }
}
