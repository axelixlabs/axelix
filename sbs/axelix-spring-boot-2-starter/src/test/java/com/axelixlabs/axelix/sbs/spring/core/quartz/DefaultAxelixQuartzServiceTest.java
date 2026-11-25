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
import java.util.List;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.quartz.CalendarIntervalScheduleBuilder;
import org.quartz.CronScheduleBuilder;
import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TimeOfDay;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.RemoteScheduler;
import org.quartz.impl.triggers.DailyTimeIntervalTriggerImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.SocketUtils;

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

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldSkipUnavailableSchedulers(boolean allUnavailable) throws Exception {
        // given.
        int port = SocketUtils.findAvailableTcpPort();
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

    @Test
    void shouldFindJobsWhenAnotherSchedulerIsUnavailable() throws Exception {
        // given.
        int port = SocketUtils.findAvailableTcpPort();
        Registry registry = LocateRegistry.createRegistry(port);
        try {
            Scheduler unavailable = new RemoteScheduler("missing", "127.0.0.1", port);
            AxelixQuartzService service =
                    new DefaultAxelixQuartzService(List.of(unavailable, standbyScheduler), new AxelixQuartzAssembler());

            // when.
            ServiceQuartzJobs result = service.getQuartzJobs("standbyScheduler");

            // then.
            assertThat(result.getJobs()).extracting(QuartzJob::getName).containsExactlyInAnyOrder("job", "orphan");
        } finally {
            UnicastRemoteObject.unexportObject(registry, true);
        }
    }

    @ParameterizedTest
    @MethodSource("misfirePolicies")
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
                    .filteredOn(value ->
                            value.getName().equals("policy") && value.getGroup().equals("service-test"))
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
    @MethodSource("jobBehaviorFlags")
    void shouldReturnJobBehaviorFlags(boolean durable, boolean persistJobData) throws Exception {
        // given.
        JobDetail job = JobBuilder.newJob(persistJobData ? PersistentJob.class : TestJob.class)
                .withIdentity("behavior", "service-test")
                .storeDurably(durable)
                .build();
        standbyScheduler.scheduleJob(
                job,
                TriggerBuilder.newTrigger()
                        .withIdentity("behavior", "service-test")
                        .forJob(job)
                        .build());
        try {
            // when.
            ServiceQuartzJobs result = quartzService.getQuartzJobs("standbyScheduler");

            // then.
            assertThat(result.getJobs())
                    .filteredOn(value -> value.getName().equals("behavior")
                            && value.getGroup().equals("service-test"))
                    .singleElement()
                    .satisfies(value -> {
                        assertThat(value.getDurable()).isEqualTo(durable);
                        assertThat(value.getPersistJobDataAfterExecution()).isEqualTo(persistJobData);
                    });
        } finally {
            standbyScheduler.deleteJob(job.getKey());
        }
    }

    @ParameterizedTest
    @MethodSource("dailyEndTimes")
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
                            .satisfies(
                                    item -> assertThat(item.getEndTimeOfDay()).isEqualTo(expected)));
        } finally {
            standbyScheduler.deleteJob(job.getKey());
        }
    }

    @Test
    void shouldRespectCustomConcurrencyFlagWithoutAnnotation() throws Exception {
        // given.
        JobDetailImpl job = new CustomJobDetail();
        job.setName("custom");
        job.setGroup("service-test");
        job.setJobClass(TestJob.class);
        job.setDurability(true);
        standbyScheduler.addJob(job, false);
        try {
            // when.
            ServiceQuartzJobs result = quartzService.getQuartzJobs("standbyScheduler");

            // then.
            assertThat(result.getJobs())
                    .filteredOn(value ->
                            value.getName().equals("custom") && value.getGroup().equals("service-test"))
                    .singleElement()
                    .satisfies(value ->
                            assertThat(value.getConcurrentExecutionDisallowed()).isTrue());
        } finally {
            standbyScheduler.deleteJob(job.getKey());
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

    @PersistJobDataAfterExecution
    public static class PersistentJob extends TestJob {}

    public static class CustomJobDetail extends JobDetailImpl {
        public boolean isConcurrentExectionDisallowed() {
            return true;
        }
    }
}
