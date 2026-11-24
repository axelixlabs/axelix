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
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.TimeOfDay;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.RemoteScheduler;
import org.quartz.impl.triggers.DailyTimeIntervalTriggerImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.TestSocketUtils;

import com.axelixlabs.axelix.common.contract.quartz.QuartzJob;
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

    @Test
    void shouldFindJobsWhenAnotherSchedulerIsUnavailable() throws Exception {
        // given.
        int port = TestSocketUtils.findAvailableTcpPort();
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

    static Stream<Arguments> dailyEndTimes() {
        return Stream.of(
                Arguments.of(null, "23:59:59"),
                Arguments.of(new TimeOfDay(18, 30, 0), "18:30"),
                Arguments.of(new TimeOfDay(23, 59, 59), "23:59:59"));
    }

    public static class CustomJobDetail extends JobDetailImpl {
        public boolean isConcurrentExectionDisallowed() {
            return true;
        }

        public boolean isConcurrentExecutionDisallowed() {
            return true;
        }
    }
}
