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
package com.axelixlabs.axelix.sbs.spring.core.quartz;

import java.util.Properties;
import java.util.TimeZone;

import org.quartz.CalendarIntervalScheduleBuilder;
import org.quartz.CronScheduleBuilder;
import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TimeOfDay;
import org.quartz.TriggerBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.quartz.AbstractQuartzSharedContextTest.QuartzTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestRestTemplateBuilder;

/**
 * Shared Spring context for Quartz integration tests.
 *
 * @author Nikita Kirillov
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "management.endpoints.web.exposure.include=axelix-quartz")
@Import({QuartzTestConfiguration.class, JwtAuthTestConfiguration.class})
abstract class AbstractQuartzSharedContextTest {

    @Autowired
    protected TestRestTemplateBuilder restTemplate;

    @Autowired
    @Qualifier("startedScheduler")
    protected Scheduler scheduler;

    @Autowired
    @Qualifier("standbyScheduler")
    protected Scheduler standbyScheduler;

    @TestConfiguration
    static class QuartzTestConfiguration {

        @Bean
        public SchedulerFactoryBean startedScheduler() {
            return scheduler("startedScheduler", true);
        }

        @Bean
        public SchedulerFactoryBean standbyScheduler() {
            JobDetail job = JobBuilder.newJob(SerialJob.class)
                    .withIdentity("job", "jobs")
                    .withDescription("Test job")
                    .requestRecovery()
                    .storeDurably()
                    .build();
            SchedulerFactoryBean factory = scheduler("standbyScheduler", false);
            factory.setJobDetails(
                    job,
                    JobBuilder.newJob(TestJob.class)
                            .withIdentity("orphan", "jobs")
                            .storeDurably()
                            .build());
            factory.setTriggers(
                    TriggerBuilder.newTrigger()
                            .withIdentity("cron", "triggers")
                            .forJob(job)
                            .withSchedule(CronScheduleBuilder.cronSchedule("0 0 12 * * ?")
                                    .inTimeZone(TimeZone.getTimeZone("Europe/Paris"))
                                    .withMisfireHandlingInstructionDoNothing())
                            .build(),
                    TriggerBuilder.newTrigger()
                            .withIdentity("simple", "triggers")
                            .forJob(job)
                            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                    .withIntervalInSeconds(30)
                                    .withRepeatCount(4))
                            .build(),
                    TriggerBuilder.newTrigger()
                            .withIdentity("calendar", "triggers")
                            .forJob(job)
                            .withSchedule(CalendarIntervalScheduleBuilder.calendarIntervalSchedule()
                                    .withIntervalInDays(1)
                                    .inTimeZone(TimeZone.getTimeZone("Europe/Paris")))
                            .build(),
                    TriggerBuilder.newTrigger()
                            .withIdentity("daily", "triggers")
                            .forJob(job)
                            .withSchedule(DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule()
                                    .withIntervalInHours(1)
                                    .onMondayThroughFriday()
                                    .startingDailyAt(TimeOfDay.hourAndMinuteOfDay(9, 0))
                                    .endingDailyAt(TimeOfDay.hourAndMinuteOfDay(18, 30))
                                    .withRepeatCount(5))
                            .build());
            return factory;
        }

        private SchedulerFactoryBean scheduler(String name, boolean autoStartup) {
            Properties properties = new Properties();
            properties.setProperty("org.quartz.threadPool.threadCount", "2");
            SchedulerFactoryBean factory = new SchedulerFactoryBean();
            factory.setSchedulerName(name);
            factory.setAutoStartup(autoStartup);
            factory.setQuartzProperties(properties);
            return factory;
        }
    }

    public static class TestJob implements Job {
        @Override
        public void execute(JobExecutionContext context) {}
    }

    @DisallowConcurrentExecution
    public static class SerialJobBase extends TestJob {}

    public static class SerialJob extends SerialJobBase {}
}
