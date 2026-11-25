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

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.quartz.CalendarIntervalScheduleBuilder;
import org.quartz.CronScheduleBuilder;
import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TimeOfDay;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.RemoteScheduler;
import org.quartz.impl.triggers.DailyTimeIntervalTriggerImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.test.util.TestSocketUtils;

import com.axelixlabs.axelix.common.contract.quartz.QuartzJob;
import com.axelixlabs.axelix.common.contract.quartz.QuartzMisfirePolicy;
import com.axelixlabs.axelix.common.contract.quartz.QuartzSchedulerMetadata;
import com.axelixlabs.axelix.common.contract.quartz.ServiceQuartzJobs;
import com.axelixlabs.axelix.common.contract.quartz.ServiceQuartzSchedulers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultAxelixQuartzService}.
 *
 * @author Nikita Kirillov
 */
class DefaultAxelixQuartzServiceTest extends AbstractQuartzSharedContextTest {

    @Autowired
    private AxelixQuartzService quartzService;

    @Nested
    class GetQuartzSchedulers {

        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        void shouldSkipUnavailableSchedulers(boolean allUnavailable) throws Exception {
            // given.
            int port = TestSocketUtils.findAvailableTcpPort();
            Registry registry = LocateRegistry.createRegistry(port);
            try {
                Scheduler unavailable = new RemoteScheduler("missing", "127.0.0.1", port);
                List<Scheduler> schedulers = allUnavailable ? List.of(unavailable) : List.of(unavailable, scheduler);
                AxelixQuartzService service = new DefaultAxelixQuartzService(schedulers, new AxelixQuartzAssembler());

                // when.
                ServiceQuartzSchedulers result = service.getQuartzSchedulers();

                // then.
                if (allUnavailable) {
                    assertThat(result.getSchedulers()).isEmpty();
                } else {
                    assertThat(result.getSchedulers())
                            .extracting(QuartzSchedulerMetadata::getSchedulerName)
                            .containsExactly("startedScheduler");
                }
            } finally {
                UnicastRemoteObject.unexportObject(registry, true);
            }
        }
    }

    @Nested
    class GetQuartzJobs {

        @ParameterizedTest
        @MethodSource("com.axelixlabs.axelix.sbs.spring.core.quartz.DefaultAxelixQuartzServiceTest#jobBehaviorFlags")
        void shouldReturnJobsWithCurrentExecutions(boolean durable, boolean persistJobData) throws Exception {
            // given.
            CountDownLatch started = new CountDownLatch(1);
            CountDownLatch release = new CountDownLatch(1);
            Properties properties = new Properties();
            properties.setProperty("org.quartz.threadPool.threadCount", "1");
            SchedulerFactoryBean factory = new SchedulerFactoryBean();
            factory.setSchedulerName("executions-test");
            factory.setQuartzProperties(properties);
            factory.setWaitForJobsToCompleteOnShutdown(true);
            factory.afterPropertiesSet();
            Scheduler localScheduler = factory.getScheduler();
            try {
                JobDetail job = JobBuilder.newJob(persistJobData ? PersistentBlockingJob.class : BlockingJob.class)
                        .withIdentity("running", "executions")
                        .storeDurably(durable)
                        .build();
                job.getJobDataMap().put("started", started);
                job.getJobDataMap().put("release", release);
                localScheduler.scheduleJob(
                        job,
                        TriggerBuilder.newTrigger()
                                .withIdentity("running-trigger", "executions")
                                .startAt(java.util.Date.from(Instant.now().plusSeconds(3600)))
                                .forJob(job)
                                .build());
                AxelixQuartzService service =
                        new DefaultAxelixQuartzService(List.of(localScheduler), new AxelixQuartzAssembler());
                factory.start();
                assertThat(service.executeQuartzJob("executions-test", "running", "executions"))
                        .isTrue();
                assertThat(started.await(10, TimeUnit.SECONDS)).isTrue();

                // when.
                ServiceQuartzJobs result = service.getQuartzJobs("executions-test");

                // then.
                assertThat(result.getJobs()).singleElement().satisfies(metadata -> {
                    assertThat(metadata.getName()).isEqualTo("running");
                    assertThat(metadata.getGroup()).isEqualTo("executions");
                    assertThat(metadata.getDurable()).isEqualTo(durable);
                    assertThat(metadata.getPersistJobDataAfterExecution()).isEqualTo(persistJobData);
                });
                assertThat(result.getExecutions()).singleElement().satisfies(execution -> {
                    assertThat(execution.getExecutionId()).isNotBlank();
                    assertThat(execution.getJobName()).isEqualTo("running");
                    assertThat(execution.getJobGroup()).isEqualTo("executions");
                    assertThat(execution.getTriggerName()).isNotBlank();
                    assertThat(execution.getTriggerGroup()).isEqualTo(Scheduler.DEFAULT_GROUP);
                    assertThat(Instant.parse(execution.getStartedAt())).isBeforeOrEqualTo(Instant.now());
                });
            } finally {
                release.countDown();
                factory.destroy();
            }
        }

        @ParameterizedTest
        @MethodSource("com.axelixlabs.axelix.sbs.spring.core.quartz.DefaultAxelixQuartzServiceTest#misfirePolicies")
        void shouldReturnMisfirePolicy(ScheduleBuilder<? extends Trigger> schedule, QuartzMisfirePolicy expected)
                throws Exception {
            // given.
            JobDetail job = JobBuilder.newJob(TestJob.class)
                    .withIdentity("policy", "service-test")
                    .build();
            standbyScheduler.scheduleJob(
                    job,
                    TriggerBuilder.newTrigger()
                            .withIdentity("policy", "service-test")
                            .forJob(job)
                            .withSchedule(schedule)
                            .build());
            try {
                // when.
                ServiceQuartzJobs result = quartzService.getQuartzJobs("standbyScheduler");

                // then.
                assertThat(result.getJobs())
                        .filteredOn(value -> value.getName().equals("policy")
                                && value.getGroup().equals("service-test"))
                        .singleElement()
                        .satisfies(value -> assertThat(value.getTriggers())
                                .singleElement()
                                .satisfies(trigger ->
                                        assertThat(trigger.getMisfirePolicy()).isEqualTo(expected)));
            } finally {
                standbyScheduler.deleteJob(job.getKey());
            }
        }

        @ParameterizedTest
        @MethodSource("com.axelixlabs.axelix.sbs.spring.core.quartz.DefaultAxelixQuartzServiceTest#dailyEndTimes")
        void shouldReturnEffectiveDailyEndTime(@Nullable TimeOfDay endTime, String expected) throws Exception {
            // given.
            JobDetail job = JobBuilder.newJob(TestJob.class)
                    .withIdentity("daily-end", "service-test")
                    .build();
            DailyTimeIntervalTriggerImpl trigger = new DailyTimeIntervalTriggerImpl();
            trigger.setName("daily-end");
            trigger.setJobKey(job.getKey());
            if (endTime != null) {
                trigger.setEndTimeOfDay(endTime);
            }
            standbyScheduler.scheduleJob(job, trigger);
            try {
                // when.
                ServiceQuartzJobs result = quartzService.getQuartzJobs("standbyScheduler");

                // then.
                assertThat(result.getJobs())
                        .filteredOn(value -> value.getName().equals("daily-end")
                                && value.getGroup().equals("service-test"))
                        .singleElement()
                        .satisfies(value -> assertThat(value.getTriggers())
                                .singleElement()
                                .satisfies(item ->
                                        assertThat(item.getEndTimeOfDay()).isEqualTo(expected)));
            } finally {
                standbyScheduler.deleteJob(job.getKey());
            }
        }

        @Test
        void shouldFindJobsWhenAnotherSchedulerIsUnavailable() throws Exception {
            // given.
            int port = TestSocketUtils.findAvailableTcpPort();
            Registry registry = LocateRegistry.createRegistry(port);
            try {
                Scheduler unavailable = new RemoteScheduler("missing", "127.0.0.1", port);
                AxelixQuartzService service = new DefaultAxelixQuartzService(
                        List.of(unavailable, standbyScheduler), new AxelixQuartzAssembler());

                // when.
                ServiceQuartzJobs result = service.getQuartzJobs("standbyScheduler");

                // then.
                assertThat(result.getJobs()).extracting(QuartzJob::getName).containsExactlyInAnyOrder("job", "orphan");
            } finally {
                UnicastRemoteObject.unexportObject(registry, true);
            }
        }
    }

    static Stream<Arguments> misfirePolicies() {
        return Stream.of(
                Arguments.of(SimpleScheduleBuilder.simpleSchedule(), QuartzMisfirePolicy.SMART_POLICY),
                Arguments.of(
                        SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionIgnoreMisfires(),
                        QuartzMisfirePolicy.IGNORE_MISFIRES),
                Arguments.of(
                        SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow(),
                        QuartzMisfirePolicy.FIRE_NOW),
                Arguments.of(
                        SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionNowWithExistingCount(),
                        QuartzMisfirePolicy.RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT),
                Arguments.of(
                        SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionNowWithRemainingCount(),
                        QuartzMisfirePolicy.RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT),
                Arguments.of(
                        SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionNextWithExistingCount(),
                        QuartzMisfirePolicy.RESCHEDULE_NEXT_WITH_EXISTING_COUNT),
                Arguments.of(
                        SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionNextWithRemainingCount(),
                        QuartzMisfirePolicy.RESCHEDULE_NEXT_WITH_REMAINING_COUNT),
                Arguments.of(
                        CronScheduleBuilder.cronSchedule("0 0 12 * * ?").withMisfireHandlingInstructionFireAndProceed(),
                        QuartzMisfirePolicy.FIRE_ONCE_NOW),
                Arguments.of(
                        CronScheduleBuilder.cronSchedule("0 0 12 * * ?").withMisfireHandlingInstructionDoNothing(),
                        QuartzMisfirePolicy.DO_NOTHING),
                Arguments.of(
                        CalendarIntervalScheduleBuilder.calendarIntervalSchedule()
                                .withMisfireHandlingInstructionFireAndProceed(),
                        QuartzMisfirePolicy.FIRE_ONCE_NOW),
                Arguments.of(
                        CalendarIntervalScheduleBuilder.calendarIntervalSchedule()
                                .withMisfireHandlingInstructionDoNothing(),
                        QuartzMisfirePolicy.DO_NOTHING),
                Arguments.of(
                        DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule()
                                .withMisfireHandlingInstructionFireAndProceed(),
                        QuartzMisfirePolicy.FIRE_ONCE_NOW),
                Arguments.of(
                        DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule()
                                .withMisfireHandlingInstructionDoNothing(),
                        QuartzMisfirePolicy.DO_NOTHING));
    }

    static Stream<Arguments> jobBehaviorFlags() {
        return Stream.of(
                Arguments.of(false, false),
                Arguments.of(false, true),
                Arguments.of(true, false),
                Arguments.of(true, true));
    }

    static Stream<Arguments> dailyEndTimes() {
        return Stream.of(
                Arguments.of(null, "23:59:59"),
                Arguments.of(new TimeOfDay(18, 30, 0), "18:30"),
                Arguments.of(new TimeOfDay(23, 59, 59), "23:59:59"));
    }

    public static class BlockingJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            CountDownLatch started =
                    (CountDownLatch) context.getMergedJobDataMap().get("started");
            CountDownLatch release =
                    (CountDownLatch) context.getMergedJobDataMap().get("release");
            started.countDown();
            try {
                if (!release.await(15, TimeUnit.SECONDS)) {
                    throw new JobExecutionException("Timed out waiting for the test to release the job");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new JobExecutionException(e);
            }
        }
    }

    @PersistJobDataAfterExecution
    public static class PersistentBlockingJob extends BlockingJob {}
}
