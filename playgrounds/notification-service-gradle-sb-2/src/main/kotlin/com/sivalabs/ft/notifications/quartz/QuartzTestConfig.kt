package com.sivalabs.ft.notifications.quartz

import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.SimpleScheduleBuilder
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Quartz job and trigger for checking the scheduler metadata reported to Axelix.
 */
@Configuration(proxyBeanMethods = false)
class QuartzTestConfig {
    @Bean
    fun loggingJobDetail(): JobDetail =
        JobBuilder
            .newJob(LoggingQuartzJob::class.java)
            .withIdentity("playground-job", "playground")
            .withDescription("Periodic job for Axelix Quartz metadata testing")
            .storeDurably()
            .build()

    @Bean
    fun loggingJobTrigger(loggingJobDetail: JobDetail): Trigger =
        TriggerBuilder
            .newTrigger()
            .withIdentity("playground-trigger", "playground")
            .forJob(loggingJobDetail)
            .withSchedule(
                SimpleScheduleBuilder
                    .simpleSchedule()
                    .withIntervalInSeconds(30)
                    .repeatForever(),
            ).build()
}
