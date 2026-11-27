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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdScheduler;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.contract.quartz.QuartzCronExpressionValidationRequest;
import com.axelixlabs.axelix.common.contract.quartz.QuartzCronExpressionValidationResponse;
import com.axelixlabs.axelix.common.contract.quartz.QuartzJob;
import com.axelixlabs.axelix.common.contract.quartz.QuartzJobExecuteRequest;
import com.axelixlabs.axelix.common.contract.quartz.QuartzJobPauseRequest;
import com.axelixlabs.axelix.common.contract.quartz.QuartzJobResumeRequest;
import com.axelixlabs.axelix.common.contract.quartz.QuartzJobsRequest;
import com.axelixlabs.axelix.common.contract.quartz.QuartzMisfirePolicy;
import com.axelixlabs.axelix.common.contract.quartz.QuartzSchedulerMetadata;
import com.axelixlabs.axelix.common.contract.quartz.QuartzTrigger;
import com.axelixlabs.axelix.common.contract.quartz.QuartzTriggerPauseRequest;
import com.axelixlabs.axelix.common.contract.quartz.QuartzTriggerResumeRequest;
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

    @Nested
    class GetQuartzSchedulers {

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

        @ProtectedEndpointTests(method = HttpMethod.GET, path = "/actuator/axelix-quartz")
        void negativeAuthTests() {}
    }

    @Nested
    class GetQuartzJobs {

        @Test
        void shouldReturnJobsAndTriggers() throws SchedulerException {
            // given.
            List<? extends Trigger> expected = standbyScheduler.getTriggersOfJob(JobKey.jobKey("job", "jobs"));

            // when.
            ResponseEntity<ServiceQuartzJobs> response = restTemplate
                    .asMaster()
                    .postForEntity(
                            "/actuator/axelix-quartz/jobs",
                            new QuartzJobsRequest().schedulerName("standbyScheduler"),
                            ServiceQuartzJobs.class);

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
        void shouldReturnEmptyJobsForSchedulerWithoutJobs() {
            // when.
            ResponseEntity<ServiceQuartzJobs> response = restTemplate
                    .asMaster()
                    .postForEntity(
                            "/actuator/axelix-quartz/jobs",
                            new QuartzJobsRequest().schedulerName("startedScheduler"),
                            ServiceQuartzJobs.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getJobs()).isEmpty();
            assertThat(response.getBody().getExecutions()).isEmpty();
        }

        @Test
        void shouldReturnNotFoundForUnknownScheduler() {
            // when.
            ResponseEntity<String> response = restTemplate
                    .asMaster()
                    .postForEntity(
                            "/actuator/axelix-quartz/jobs",
                            new QuartzJobsRequest().schedulerName("missing"),
                            String.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @ProtectedEndpointTests(
                method = HttpMethod.POST,
                path = "/actuator/axelix-quartz/jobs",
                jsonBody = "{\"schedulerName\":\"standbyScheduler\"}")
        void negativeJobsAuthTests() {}
    }

    @Nested
    class ValidateCronExpression {

        @Test
        void shouldAcceptQuartzCronExpressionWithYearField() {
            // given.
            QuartzCronExpressionValidationRequest request =
                    new QuartzCronExpressionValidationRequest().cronExpression("0 0 12 * * ? 2027");

            // when.
            ResponseEntity<QuartzCronExpressionValidationResponse> response = restTemplate
                    .asMaster()
                    .postForEntity(
                            "/actuator/axelix-quartz/validateCronExpression",
                            request,
                            QuartzCronExpressionValidationResponse.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getValid()).isTrue();
        }

        @Test
        void shouldRejectInvalidQuartzCronExpression() {
            // given.
            QuartzCronExpressionValidationRequest request =
                    new QuartzCronExpressionValidationRequest().cronExpression("not a cron expression");

            // when.
            ResponseEntity<QuartzCronExpressionValidationResponse> response = restTemplate
                    .asMaster()
                    .postForEntity(
                            "/actuator/axelix-quartz/validateCronExpression",
                            request,
                            QuartzCronExpressionValidationResponse.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getValid()).isFalse();
        }

        @Test
        void shouldRejectIncompleteRequest() {
            // given.
            Map<String, String> request = Map.of();

            // when.
            ResponseEntity<Void> response = restTemplate
                    .asMaster()
                    .postForEntity("/actuator/axelix-quartz/validateCronExpression", request, Void.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    class ExecuteQuartzJob {

        @Test
        void shouldRequestJobExecution() throws SchedulerException {
            // given.
            JobDetail job = JobBuilder.newJob(TestJob.class)
                    .withIdentity("manual", "endpoint-test")
                    .storeDurably()
                    .build();
            standbyScheduler.addJob(job, false);
            QuartzJobExecuteRequest request = new QuartzJobExecuteRequest()
                    .schedulerName("standbyScheduler")
                    .jobName("manual")
                    .jobGroup("endpoint-test");
            try {
                // when.
                ResponseEntity<Void> response =
                        restTemplate.asMaster().postForEntity("/actuator/axelix-quartz/execute", request, Void.class);

                // then.
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                assertThat(standbyScheduler.getTriggersOfJob(job.getKey()))
                        .singleElement()
                        .satisfies(trigger -> assertThat(trigger.getJobKey()).isEqualTo(job.getKey()));
            } finally {
                standbyScheduler.deleteJob(job.getKey());
            }
        }

        @ParameterizedTest
        @MethodSource("com.axelixlabs.axelix.sbs.spring.core.quartz.AxelixQuartzEndpointTest#missingJobs")
        void shouldReturnNotFoundForExecution(String schedulerName, String jobName, String jobGroup) {
            // given.
            QuartzJobExecuteRequest request = new QuartzJobExecuteRequest()
                    .schedulerName(schedulerName)
                    .jobName(jobName)
                    .jobGroup(jobGroup);

            // when.
            ResponseEntity<Void> response =
                    restTemplate.asMaster().postForEntity("/actuator/axelix-quartz/execute", request, Void.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void shouldRejectIncompleteRequest() {
            // given.
            Map<String, String> request = Map.of("schedulerName", "standbyScheduler", "jobName", "job");

            // when.
            ResponseEntity<Void> response =
                    restTemplate.asMaster().postForEntity("/actuator/axelix-quartz/execute", request, Void.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @ProtectedEndpointTests(
                method = HttpMethod.POST,
                path = "/actuator/axelix-quartz/execute",
                jsonBody = "{\"schedulerName\":\"standbyScheduler\",\"jobName\":\"job\",\"jobGroup\":\"jobs\"}")
        void negativeExecuteAuthTests() {}
    }

    @Nested
    class PauseQuartzTrigger {

        @Test
        void shouldPauseTrigger() throws SchedulerException {
            // given.
            JobDetail job = JobBuilder.newJob(TestJob.class)
                    .withIdentity("pause", "endpoint-test")
                    .build();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("pause", "endpoint-test")
                    .forJob(job)
                    .build();
            standbyScheduler.scheduleJob(job, trigger);
            QuartzTriggerPauseRequest request = new QuartzTriggerPauseRequest()
                    .schedulerName("standbyScheduler")
                    .triggerName("pause")
                    .triggerGroup("endpoint-test");
            try {
                // when.
                ResponseEntity<Void> response = restTemplate
                        .asMaster()
                        .postForEntity("/actuator/axelix-quartz/pauseTrigger", request, Void.class);

                // then.
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                assertThat(standbyScheduler.getTriggerState(trigger.getKey())).isEqualTo(Trigger.TriggerState.PAUSED);
                assertThat(standbyScheduler.checkExists(job.getKey())).isTrue();
                assertThat(standbyScheduler.getTriggersOfJob(job.getKey())).hasSize(1);
                assertThat(standbyScheduler.getTriggerState(TriggerKey.triggerKey("cron", "triggers")))
                        .isEqualTo(Trigger.TriggerState.NORMAL);
            } finally {
                standbyScheduler.deleteJob(job.getKey());
            }
        }

        @ParameterizedTest
        @MethodSource("com.axelixlabs.axelix.sbs.spring.core.quartz.AxelixQuartzEndpointTest#missingTriggers")
        void shouldReturnNotFoundForTrigger(String schedulerName, String triggerName, String triggerGroup) {
            // given.
            QuartzTriggerPauseRequest request = new QuartzTriggerPauseRequest()
                    .schedulerName(schedulerName)
                    .triggerName(triggerName)
                    .triggerGroup(triggerGroup);

            // when.
            ResponseEntity<Void> response =
                    restTemplate.asMaster().postForEntity("/actuator/axelix-quartz/pauseTrigger", request, Void.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void shouldRejectIncompleteRequest() {
            // given.
            Map<String, String> request = Map.of("triggerName", "cron", "triggerGroup", "triggers");

            // when.
            ResponseEntity<Void> response =
                    restTemplate.asMaster().postForEntity("/actuator/axelix-quartz/pauseTrigger", request, Void.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @ProtectedEndpointTests(
                method = HttpMethod.POST,
                path = "/actuator/axelix-quartz/pauseTrigger",
                jsonBody =
                        "{\"schedulerName\":\"standbyScheduler\",\"triggerName\":\"cron\",\"triggerGroup\":\"triggers\"}")
        void negativeTriggerPauseAuthTests() {}
    }

    @Nested
    class PauseQuartzJob {

        @Test
        void shouldPauseAllJobTriggersWithoutAffectingOtherJobs() throws SchedulerException {
            // given.
            JobDetail job = JobBuilder.newJob(TestJob.class)
                    .withIdentity("pause-job", "endpoint-test")
                    .build();
            Trigger firstTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("pause-job-first", "endpoint-test")
                    .forJob(job)
                    .build();
            Trigger secondTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("pause-job-second", "endpoint-test")
                    .forJob(job)
                    .build();
            standbyScheduler.scheduleJob(job, Set.of(firstTrigger, secondTrigger), false);
            QuartzJobPauseRequest request = new QuartzJobPauseRequest()
                    .schedulerName("standbyScheduler")
                    .jobName("pause-job")
                    .jobGroup("endpoint-test");
            try {
                // when.
                ResponseEntity<Void> response =
                        restTemplate.asMaster().postForEntity("/actuator/axelix-quartz/pauseJob", request, Void.class);

                // then.
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                assertThat(standbyScheduler.getTriggerState(firstTrigger.getKey()))
                        .isEqualTo(Trigger.TriggerState.PAUSED);
                assertThat(standbyScheduler.getTriggerState(secondTrigger.getKey()))
                        .isEqualTo(Trigger.TriggerState.PAUSED);
                assertThat(standbyScheduler.getTriggerState(TriggerKey.triggerKey("cron", "triggers")))
                        .isEqualTo(Trigger.TriggerState.NORMAL);
            } finally {
                standbyScheduler.deleteJob(job.getKey());
            }
        }

        @ParameterizedTest
        @MethodSource("com.axelixlabs.axelix.sbs.spring.core.quartz.AxelixQuartzEndpointTest#missingJobs")
        void shouldReturnNotFoundForJob(String schedulerName, String jobName, String jobGroup) {
            // given.
            QuartzJobPauseRequest request = new QuartzJobPauseRequest()
                    .schedulerName(schedulerName)
                    .jobName(jobName)
                    .jobGroup(jobGroup);

            // when.
            ResponseEntity<Void> response =
                    restTemplate.asMaster().postForEntity("/actuator/axelix-quartz/pauseJob", request, Void.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void shouldRejectIncompleteRequest() {
            // given.
            Map<String, String> request = Map.of("schedulerName", "standbyScheduler", "jobName", "job");

            // when.
            ResponseEntity<Void> response =
                    restTemplate.asMaster().postForEntity("/actuator/axelix-quartz/pauseJob", request, Void.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @ProtectedEndpointTests(
                method = HttpMethod.POST,
                path = "/actuator/axelix-quartz/pauseJob",
                jsonBody = "{\"schedulerName\":\"standbyScheduler\",\"jobName\":\"job\",\"jobGroup\":\"jobs\"}")
        void negativeQuartzJobPauseAuthTests() {}
    }

    @Nested
    class ResumeQuartzTrigger {

        @Test
        void shouldResumeTrigger() throws SchedulerException {
            // given.
            JobDetail job = JobBuilder.newJob(TestJob.class)
                    .withIdentity("pause", "endpoint-test")
                    .build();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("pause", "endpoint-test")
                    .forJob(job)
                    .build();
            standbyScheduler.scheduleJob(job, trigger);
            standbyScheduler.pauseTrigger(trigger.getKey());
            QuartzTriggerResumeRequest request = new QuartzTriggerResumeRequest()
                    .schedulerName("standbyScheduler")
                    .triggerName("pause")
                    .triggerGroup("endpoint-test");
            try {
                // when.
                ResponseEntity<Void> response = restTemplate
                        .asMaster()
                        .postForEntity("/actuator/axelix-quartz/resumeTrigger", request, Void.class);

                // then.
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                assertThat(standbyScheduler.getTriggerState(trigger.getKey())).isEqualTo(Trigger.TriggerState.NORMAL);
                assertThat(standbyScheduler.checkExists(job.getKey())).isTrue();
                assertThat(standbyScheduler.getTriggersOfJob(job.getKey())).hasSize(1);
                assertThat(standbyScheduler.getTriggerState(TriggerKey.triggerKey("cron", "triggers")))
                        .isEqualTo(Trigger.TriggerState.NORMAL);
            } finally {
                standbyScheduler.deleteJob(job.getKey());
            }
        }

        @ParameterizedTest
        @MethodSource("com.axelixlabs.axelix.sbs.spring.core.quartz.AxelixQuartzEndpointTest#missingTriggers")
        void shouldReturnNotFoundForTrigger(String schedulerName, String triggerName, String triggerGroup) {
            // given.
            QuartzTriggerResumeRequest request = new QuartzTriggerResumeRequest()
                    .schedulerName(schedulerName)
                    .triggerName(triggerName)
                    .triggerGroup(triggerGroup);

            // when.
            ResponseEntity<Void> response =
                    restTemplate.asMaster().postForEntity("/actuator/axelix-quartz/resumeTrigger", request, Void.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void shouldRejectIncompleteRequest() {
            // given.
            Map<String, String> request = Map.of("triggerName", "cron", "triggerGroup", "triggers");

            // when.
            ResponseEntity<Void> response =
                    restTemplate.asMaster().postForEntity("/actuator/axelix-quartz/resumeTrigger", request, Void.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @ProtectedEndpointTests(
                method = HttpMethod.POST,
                path = "/actuator/axelix-quartz/resumeTrigger",
                jsonBody =
                        "{\"schedulerName\":\"standbyScheduler\",\"triggerName\":\"cron\",\"triggerGroup\":\"triggers\"}")
        void negativeTriggerResumeAuthTests() {}
    }

    @Nested
    class ResumeQuartzJob {

        @Test
        void shouldResumeAllJobTriggersWithoutAffectingOtherJobs() throws SchedulerException {
            // given.
            JobDetail job = JobBuilder.newJob(TestJob.class)
                    .withIdentity("resume-job", "endpoint-test")
                    .build();
            Trigger firstTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("resume-job-first", "endpoint-test")
                    .forJob(job)
                    .build();
            Trigger secondTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("resume-job-second", "endpoint-test")
                    .forJob(job)
                    .build();
            JobDetail unrelatedJob = JobBuilder.newJob(TestJob.class)
                    .withIdentity("unrelated-resume-job", "endpoint-test")
                    .build();
            Trigger unrelatedTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("unrelated-resume-job-trigger", "endpoint-test")
                    .forJob(unrelatedJob)
                    .build();
            QuartzJobResumeRequest request = new QuartzJobResumeRequest()
                    .schedulerName("standbyScheduler")
                    .jobName("resume-job")
                    .jobGroup("endpoint-test");
            try {
                standbyScheduler.scheduleJob(job, Set.of(firstTrigger, secondTrigger), false);
                standbyScheduler.scheduleJob(unrelatedJob, unrelatedTrigger);
                standbyScheduler.pauseJob(job.getKey());
                standbyScheduler.pauseJob(unrelatedJob.getKey());

                // when.
                ResponseEntity<Void> response =
                        restTemplate.asMaster().postForEntity("/actuator/axelix-quartz/resumeJob", request, Void.class);

                // then.
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                assertThat(standbyScheduler.getTriggerState(firstTrigger.getKey()))
                        .isEqualTo(Trigger.TriggerState.NORMAL);
                assertThat(standbyScheduler.getTriggerState(secondTrigger.getKey()))
                        .isEqualTo(Trigger.TriggerState.NORMAL);
                assertThat(standbyScheduler.getTriggerState(unrelatedTrigger.getKey()))
                        .isEqualTo(Trigger.TriggerState.PAUSED);
            } finally {
                standbyScheduler.deleteJob(job.getKey());
                standbyScheduler.deleteJob(unrelatedJob.getKey());
            }
        }

        @ParameterizedTest
        @MethodSource("com.axelixlabs.axelix.sbs.spring.core.quartz.AxelixQuartzEndpointTest#missingJobs")
        void shouldReturnNotFoundForJob(String schedulerName, String jobName, String jobGroup) {
            // given.
            QuartzJobResumeRequest request = new QuartzJobResumeRequest()
                    .schedulerName(schedulerName)
                    .jobName(jobName)
                    .jobGroup(jobGroup);

            // when.
            ResponseEntity<Void> response =
                    restTemplate.asMaster().postForEntity("/actuator/axelix-quartz/resumeJob", request, Void.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void shouldRejectIncompleteRequest() {
            // given.
            Map<String, String> request = Map.of("schedulerName", "standbyScheduler", "jobName", "job");

            // when.
            ResponseEntity<Void> response =
                    restTemplate.asMaster().postForEntity("/actuator/axelix-quartz/resumeJob", request, Void.class);

            // then.
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @ProtectedEndpointTests(
                method = HttpMethod.POST,
                path = "/actuator/axelix-quartz/resumeJob",
                jsonBody = "{\"schedulerName\":\"standbyScheduler\",\"jobName\":\"job\",\"jobGroup\":\"jobs\"}")
        void negativeQuartzJobResumeAuthTests() {}
    }

    @Test
    void shouldRejectUnknownTriggerOperation() {
        // given.
        QuartzTriggerPauseRequest request = new QuartzTriggerPauseRequest()
                .schedulerName("standbyScheduler")
                .triggerName("cron")
                .triggerGroup("triggers");

        // when.
        ResponseEntity<Void> response =
                restTemplate.asMaster().postForEntity("/actuator/axelix-quartz/unknown", request, Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    static Stream<Arguments> missingTriggers() {
        return Stream.of(
                Arguments.of("missing", "cron", "triggers"),
                Arguments.of("standbyScheduler", "missing", "triggers"),
                Arguments.of("standbyScheduler", "cron", "missing"));
    }

    static Stream<Arguments> missingJobs() {
        return Stream.of(
                Arguments.of("missing", "job", "jobs"),
                Arguments.of("standbyScheduler", "missing", "jobs"),
                Arguments.of("standbyScheduler", "job", "missing"));
    }
}
