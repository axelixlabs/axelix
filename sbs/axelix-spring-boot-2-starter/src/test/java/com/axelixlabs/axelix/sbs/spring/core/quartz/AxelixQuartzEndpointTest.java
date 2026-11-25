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

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.quartz.DailyTimeIntervalTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;
import org.quartz.Trigger;
import org.quartz.impl.StdScheduler;
import org.quartz.impl.triggers.DailyTimeIntervalTriggerImpl;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.contract.quartz.QuartzJob;
import com.axelixlabs.axelix.common.contract.quartz.QuartzMisfirePolicy;
import com.axelixlabs.axelix.common.contract.quartz.QuartzSchedulerMetadata;
import com.axelixlabs.axelix.common.contract.quartz.QuartzTrigger;
import com.axelixlabs.axelix.common.contract.quartz.QuartzTriggerType;
import com.axelixlabs.axelix.common.contract.quartz.ServiceQuartzJobs;
import com.axelixlabs.axelix.common.contract.quartz.ServiceQuartzSchedulers;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.sbs.spring.core.utils.auth.ProtectedEndpointTests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Integration tests for {@link AxelixQuartzEndpoint}.
 *
 * @author Nikita Kirillov
 */
class AxelixQuartzEndpointTest extends AbstractQuartzSharedContextTest {

    @Test
    void shouldReturnQuartzSchedulers() throws SchedulerException {
        // given.
        SchedulerMetaData expected = scheduler.getMetaData();

        // when.
        ResponseEntity<ServiceQuartzSchedulers> response =
                restTemplate.asMaster().getForEntity("/actuator/axelix-quartz", ServiceQuartzSchedulers.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getSchedulers())
                .extracting(
                        QuartzSchedulerMetadata::getSchedulerName,
                        QuartzSchedulerMetadata::getStarted,
                        QuartzSchedulerMetadata::getInStandbyMode,
                        QuartzSchedulerMetadata::getShutdown,
                        QuartzSchedulerMetadata::getStartedAt)
                .containsExactlyInAnyOrder(
                        tuple(
                                "startedScheduler",
                                true,
                                false,
                                false,
                                expected.getRunningSince().toInstant().toString()),
                        tuple("standbyScheduler", false, true, false, null));
        assertThat(response.getBody().getSchedulers()).allSatisfy(metadata -> {
            assertThat(metadata.getSchedulerInstanceId()).isEqualTo("NON_CLUSTERED");
            assertThat(metadata.getVersion()).isEqualTo(expected.getVersion());
            assertThat(metadata.getSchedulerClassName()).isEqualTo(StdScheduler.class.getName());
            assertThat(metadata.getSchedulerRemote()).isFalse();
            assertThat(metadata.getNumberOfJobsExecuted()).isZero();
            assertThat(metadata.getJobStoreClassName()).isEqualTo(RAMJobStore.class.getName());
            assertThat(metadata.getJobStoreSupportsPersistence()).isFalse();
            assertThat(metadata.getJobStoreClustered()).isFalse();
            assertThat(metadata.getThreadPoolClassName()).isEqualTo(SimpleThreadPool.class.getName());
            assertThat(metadata.getThreadPoolSize()).isEqualTo(2);
        });
    }

    @Test
    void shouldReturnJobsAndTriggers() throws SchedulerException {
        // given.
        List<? extends Trigger> expected = standbyScheduler.getTriggersOfJob(JobKey.jobKey("job", "jobs"));

        // when.
        ResponseEntity<ServiceQuartzJobs> response = restTemplate
                .asMaster()
                .getForEntity("/actuator/axelix-quartz/standbyScheduler", ServiceQuartzJobs.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<QuartzJob> jobs = response.getBody().getJobs();
        assertThat(jobs)
                .extracting(
                        QuartzJob::getName,
                        QuartzJob::getGroup,
                        QuartzJob::getClassName,
                        QuartzJob::getDescription,
                        QuartzJob::getConcurrentExecutionDisallowed,
                        QuartzJob::getRequestsRecovery)
                .containsExactlyInAnyOrder(
                        tuple("job", "jobs", SerialJob.class.getName(), "Test job", true, true),
                        tuple("orphan", "jobs", TestJob.class.getName(), null, false, false));
        assertThat(jobs)
                .filteredOn("name", "orphan")
                .singleElement()
                .satisfies(job -> assertThat(job.getTriggers()).isEmpty());
        List<QuartzTrigger> triggers =
                jobs.stream().flatMap(job -> job.getTriggers().stream()).collect(Collectors.toList());
        assertThat(triggers)
                .extracting(
                        QuartzTrigger::getName,
                        QuartzTrigger::getGroup,
                        QuartzTrigger::getType,
                        QuartzTrigger::getState,
                        QuartzTrigger::getMisfirePolicy)
                .containsExactlyInAnyOrder(
                        tuple("cron", "triggers", QuartzTriggerType.CRON, "NORMAL", QuartzMisfirePolicy.DO_NOTHING),
                        tuple(
                                "simple",
                                "triggers",
                                QuartzTriggerType.SIMPLE,
                                "NORMAL",
                                QuartzMisfirePolicy.SMART_POLICY),
                        tuple(
                                "calendar",
                                "triggers",
                                QuartzTriggerType.CALENDAR_INTERVAL,
                                "NORMAL",
                                QuartzMisfirePolicy.SMART_POLICY),
                        tuple(
                                "daily",
                                "triggers",
                                QuartzTriggerType.DAILY_TIME_INTERVAL,
                                "NORMAL",
                                QuartzMisfirePolicy.SMART_POLICY));
        assertThat(triggers)
                .filteredOn("type", QuartzTriggerType.CRON)
                .singleElement()
                .satisfies(trigger -> {
                    assertThat(trigger.getCronExpression()).isEqualTo("0 0 12 * * ?");
                    assertThat(trigger.getTimeZone()).isEqualTo("Europe/Paris");
                    assertThat(trigger.getInterval()).isNull();
                });
        assertThat(triggers)
                .filteredOn("type", QuartzTriggerType.SIMPLE)
                .singleElement()
                .satisfies(trigger -> {
                    assertThat(trigger.getInterval()).isEqualTo(30_000L);
                    assertThat(trigger.getIntervalUnit()).isEqualTo("MILLISECOND");
                    assertThat(trigger.getRepeatCount()).isEqualTo(4);
                    assertThat(trigger.getCronExpression()).isNull();
                });
        assertThat(triggers)
                .filteredOn("type", QuartzTriggerType.CALENDAR_INTERVAL)
                .singleElement()
                .satisfies(trigger -> {
                    assertThat(trigger.getInterval()).isEqualTo(1L);
                    assertThat(trigger.getIntervalUnit()).isEqualTo("DAY");
                    assertThat(trigger.getTimeZone()).isEqualTo("Europe/Paris");
                });
        assertThat(triggers)
                .filteredOn("type", QuartzTriggerType.DAILY_TIME_INTERVAL)
                .singleElement()
                .satisfies(trigger -> {
                    assertThat(trigger.getInterval()).isEqualTo(1L);
                    assertThat(trigger.getIntervalUnit()).isEqualTo("HOUR");
                    assertThat(trigger.getRepeatCount()).isEqualTo(5);
                    assertThat(trigger.getDaysOfWeek()).containsExactly(2, 3, 4, 5, 6);
                    assertThat(trigger.getStartTimeOfDay()).isEqualTo("09:00");
                    assertThat(trigger.getEndTimeOfDay()).isEqualTo("18:30");
                });
        for (QuartzTrigger trigger : triggers) {
            Trigger original = expected.stream()
                    .filter(value -> value.getKey().getName().equals(trigger.getName()))
                    .findFirst()
                    .orElseThrow();
            assertThat(trigger.getPreviousFireTime()).isNull();
            assertThat(trigger.getNextFireTime())
                    .isEqualTo(original.getNextFireTime().toInstant().toString());
        }
    }

    @Test
    void shouldReturnEndOfDayForDailyTriggerWithoutEndTime() throws SchedulerException {
        // given.
        JobDetail job = JobBuilder.newJob(TestJob.class)
                .withIdentity("daily-without-end", "regression")
                .build();
        DailyTimeIntervalTriggerImpl trigger = new DailyTimeIntervalTriggerImpl();
        trigger.setName("daily-without-end");
        trigger.setJobKey(job.getKey());
        standbyScheduler.scheduleJob(job, trigger);
        try {
            // when.
            ResponseEntity<ServiceQuartzJobs> response = restTemplate
                    .asMaster()
                    .getForEntity("/actuator/axelix-quartz/standbyScheduler", ServiceQuartzJobs.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(((DailyTimeIntervalTrigger) standbyScheduler.getTrigger(trigger.getKey())).getEndTimeOfDay())
                    .isNull();
            assertThat(response.getBody().getJobs())
                    .filteredOn(value -> value.getName().equals("daily-without-end")
                            && value.getGroup().equals("regression"))
                    .singleElement()
                    .satisfies(value -> assertThat(value.getTriggers())
                            .singleElement()
                            .satisfies(result -> {
                                assertThat(result.getType()).isEqualTo(QuartzTriggerType.DAILY_TIME_INTERVAL);
                                assertThat(result.getEndTimeOfDay()).isEqualTo("23:59:59");
                            }));
        } finally {
            standbyScheduler.deleteJob(job.getKey());
        }
    }

    @Test
    void shouldReturnEmptyJobsForSchedulerWithoutJobs() {
        // when.
        ResponseEntity<ServiceQuartzJobs> response = restTemplate
                .asMaster()
                .getForEntity("/actuator/axelix-quartz/startedScheduler", ServiceQuartzJobs.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getJobs()).isEmpty();
    }

    @Test
    void shouldReturnNotFoundForUnknownScheduler() {
        // when.
        ResponseEntity<String> response =
                restTemplate.asMaster().getForEntity("/actuator/axelix-quartz/missing", String.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @ProtectedEndpointTests(method = HttpMethod.GET, path = "/actuator/axelix-quartz/standbyScheduler")
    void negativeJobsAuthTests() {}

    @ProtectedEndpointTests(method = HttpMethod.GET, path = "/actuator/axelix-quartz")
    void negativeAuthTests() {}
}
