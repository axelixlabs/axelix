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
import java.util.Properties;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.quartz.CalendarIntervalScheduleBuilder;
import org.quartz.CronScheduleBuilder;
import org.quartz.DailyTimeIntervalScheduleBuilder;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerMetaData;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TimeOfDay;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.RemoteScheduler;
import org.quartz.impl.StdScheduler;
import org.quartz.simpl.RAMJobStore;
import org.quartz.simpl.SimpleThreadPool;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.test.util.TestSocketUtils;

import com.axelixlabs.axelix.common.contract.quartz.QuartzJob;
import com.axelixlabs.axelix.common.contract.quartz.QuartzSchedulerMetadata;
import com.axelixlabs.axelix.common.contract.quartz.QuartzTrigger;
import com.axelixlabs.axelix.common.contract.quartz.QuartzTriggerType;
import com.axelixlabs.axelix.common.contract.quartz.ServiceQuartzJobs;
import com.axelixlabs.axelix.common.contract.quartz.ServiceQuartzSchedulers;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.sbs.spring.core.IgnoreTestContextArchitecture;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.quartz.AxelixQuartzEndpointTest.QuartzTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.sbs.spring.core.utils.auth.ProtectedEndpointTests;

import static com.axelixlabs.axelix.sbs.spring.core.IgnoreTestContextArchitecture.NO_SIBLINGS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Integration tests for {@link AxelixQuartzEndpoint}.
 *
 * @author Nikita Kirillov
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "management.endpoints.web.exposure.include=axelix-quartz")
@Import({QuartzTestConfiguration.class, JwtAuthTestConfiguration.class})
@IgnoreTestContextArchitecture(reason = NO_SIBLINGS)
class AxelixQuartzEndpointTest {

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    @Autowired
    @Qualifier("startedScheduler")
    private Scheduler scheduler;

    @Autowired
    @Qualifier("standbyScheduler")
    private Scheduler standbyScheduler;

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
                jobs.stream().flatMap(job -> job.getTriggers().stream()).toList();
        assertThat(triggers)
                .extracting(
                        QuartzTrigger::getName,
                        QuartzTrigger::getGroup,
                        QuartzTrigger::getType,
                        QuartzTrigger::getState)
                .containsExactlyInAnyOrder(
                        tuple("cron", "triggers", QuartzTriggerType.CRON, "NORMAL"),
                        tuple("simple", "triggers", QuartzTriggerType.SIMPLE, "NORMAL"),
                        tuple("calendar", "triggers", QuartzTriggerType.CALENDAR_INTERVAL, "NORMAL"),
                        tuple("daily", "triggers", QuartzTriggerType.DAILY_TIME_INTERVAL, "NORMAL"));
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
    public static class SerialJob extends TestJob {}
}
