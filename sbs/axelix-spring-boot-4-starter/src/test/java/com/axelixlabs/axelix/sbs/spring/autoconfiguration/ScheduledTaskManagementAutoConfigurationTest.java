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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import org.springframework.boot.LazyInitializationBeanFactoryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.boot.micrometer.observation.autoconfigure.ObservationAutoConfiguration;
import org.springframework.boot.micrometer.observation.autoconfigure.ScheduledTasksObservationAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TaskManagementConfigUtils;

import com.axelixlabs.axelix.sbs.spring.core.contract.metadata.ScheduledTaskExecution;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.AxelixScheduledAnnotationBeanPostProcessor;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.AxelixScheduledTasksEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.IntervalBasedTaskRescheduler;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ManagedScheduledTask;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTaskExecutionHistory;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTaskService;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTasksAssembler;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.ScheduledTasksRegistry;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.TaskRescheduler;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.TaskSchedulerProvider;
import com.axelixlabs.axelix.sbs.spring.core.scheduled.TriggerBasedTaskRescheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Integration tests for {@link ScheduledTaskManagementAutoConfiguration}
 *
 * @since 10.02.2026
 * @author Nikita Kirillov
 * @author Dmitry Kiselev
 * @author Vyacheslav Yanin
 * @author Dmitry Mazurov
 */
class ScheduledTaskManagementAutoConfigurationTest {

    private static final String ENDPOINT_EXPOSURE_PROPERTY =
            "management.endpoints.web.exposure.include=axelix-scheduled-tasks";
    private static final String TASK_SCHEDULER_BEAN_NAME = "taskScheduler";
    private static final String SPRING_BOOT_SCHEDULER_THREAD_PREFIX = "scheduling-";
    private static final String USER_TASK_SCHEDULER_BEAN_NAME = "userTaskScheduler";
    private static final String QUALIFIED_SCHEDULER_BEAN_NAME = "qualifiedScheduler";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withPropertyValues(ENDPOINT_EXPOSURE_PROPERTY)
            .withUserConfiguration(EnableSchedulingConfig.class)
            .withConfiguration(AutoConfigurations.of(
                    TaskSchedulingAutoConfiguration.class,
                    TaskExecutionAutoConfiguration.class,
                    ScheduledTaskManagementAutoConfiguration.class));

    @Test
    void shouldCreateAllBeansInDefaultScenario() {
        // given: ApplicationContextRunner configured with required properties and scheduling enabled

        // when
        contextRunner.run(context -> {
            // then
            assertThat(context).hasSingleBean(ScheduledTasksRegistry.class);
            assertThat(context).hasSingleBean(ScheduledTaskService.class);
            assertThat(context).hasSingleBean(ScheduledTasksAssembler.class);
            assertThat(context).hasSingleBean(AxelixScheduledTasksEndpoint.class);
            assertThat(context).hasSingleBean(ScheduledTaskExecutionHistory.class);

            assertThat(context).getBeans(TaskRescheduler.class).hasSize(2);
            assertThat(context).hasSingleBean(IntervalBasedTaskRescheduler.class);
            assertThat(context).hasSingleBean(TriggerBasedTaskRescheduler.class);

            // Axelix resources must not make the Spring Boot's own infrastructure beans back off
            assertThat(context).hasSingleBean(ThreadPoolTaskExecutor.class);
            assertThat(context).hasSingleBean(ThreadPoolTaskScheduler.class);
            assertThat(context).hasBean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME);
            assertThat(context).hasBean(TASK_SCHEDULER_BEAN_NAME);
        });
    }

    @Test
    void shouldNotActivateAutoConfiguration_withoutRequiredProperty() {
        // given
        new ApplicationContextRunner()
                .withUserConfiguration(EnableSchedulingConfig.class)
                .withConfiguration(AutoConfigurations.of(
                        TaskSchedulingAutoConfiguration.class,
                        TaskExecutionAutoConfiguration.class,
                        ScheduledTaskManagementAutoConfiguration.class))
                // when
                .run(context -> {
                    // then
                    assertThat(context).doesNotHaveBean(ScheduledTaskManagementAutoConfiguration.class);
                    assertThat(context).doesNotHaveBean(ScheduledTasksRegistry.class);
                    assertThat(context).doesNotHaveBean(ScheduledTaskService.class);
                    assertThat(context).doesNotHaveBean(AxelixScheduledTasksEndpoint.class);
                    assertThat(context).doesNotHaveBean(ScheduledTaskExecutionHistory.class);
                    assertThat(context).hasSingleBean(ThreadPoolTaskExecutor.class);
                    assertThat(context).hasSingleBean(ThreadPoolTaskScheduler.class);
                    assertThat(context).hasBean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME);
                    assertThat(context).hasBean(TASK_SCHEDULER_BEAN_NAME);
                });
    }

    @Test
    void shouldCreateReschedulers_whenSchedulingNotEnabled() {
        // given
        new ApplicationContextRunner()
                .withPropertyValues(ENDPOINT_EXPOSURE_PROPERTY)
                .withConfiguration(AutoConfigurations.of(ScheduledTaskManagementAutoConfiguration.class))
                // when
                .run(context -> {
                    // then
                    // read path is available even without @EnableScheduling
                    assertThat(context).hasSingleBean(ScheduledTasksRegistry.class);
                    assertThat(context).hasSingleBean(ScheduledTaskService.class);
                    assertThat(context).hasSingleBean(ScheduledTasksAssembler.class);
                    assertThat(context).hasSingleBean(AxelixScheduledTasksEndpoint.class);
                    assertThat(context).doesNotHaveBean(ThreadPoolTaskExecutor.class);
                    assertThat(context).doesNotHaveBean(ThreadPoolTaskScheduler.class);
                    assertThat(context)
                            .doesNotHaveBean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME);
                    assertThat(context).doesNotHaveBean(TASK_SCHEDULER_BEAN_NAME);

                    // reschedulers take the scheduler of the application lazily, so they are
                    // created even without @EnableScheduling
                    assertThat(context).getBeans(TaskRescheduler.class).hasSize(2);
                    assertThat(context).hasSingleBean(IntervalBasedTaskRescheduler.class);
                    assertThat(context).hasSingleBean(TriggerBasedTaskRescheduler.class);

                    // there is no registrar without @EnableScheduling, so the scheduler is not available
                    TaskSchedulerProvider provider = context.getBean(TaskSchedulerProvider.class);
                    assertThatThrownBy(provider::getTaskScheduler).isInstanceOf(IllegalStateException.class);
                });
    }

    @Test // GH-1726
    void shouldRescheduleOnApplicationScheduler_whenApplicationUsesSpringBootScheduler() {
        contextRunner.run(context -> assertReschedulesOnThread(context, SPRING_BOOT_SCHEDULER_THREAD_PREFIX));
    }

    @Test // GH-1726
    void shouldRescheduleOnApplicationScheduler_whenLazyInitializationIsEnabled() {
        contextRunner
                .withInitializer(context ->
                        context.addBeanFactoryPostProcessor(new LazyInitializationBeanFactoryPostProcessor()))
                .run(context -> assertReschedulesOnThread(context, SPRING_BOOT_SCHEDULER_THREAD_PREFIX));
    }

    @Test // GH-1726
    void shouldRescheduleOnApplicationScheduler_whenUserDeclaresTaskScheduler() {
        // given: the user declares own TaskScheduler

        contextRunner
                .withUserConfiguration(ThreadPoolTaskSchedulerConfig.class)
                // when
                .run(context -> {
                    // then
                    assertThat(context).hasSingleBean(ThreadPoolTaskScheduler.class);
                    assertThat(context).hasBean(USER_TASK_SCHEDULER_BEAN_NAME);
                    assertThat(context).doesNotHaveBean(TASK_SCHEDULER_BEAN_NAME);

                    assertReschedulesOnThread(context, USER_TASK_SCHEDULER_BEAN_NAME + "-");
                });
    }

    @Test // GH-1726
    void shouldRescheduleOnQualifiedScheduler_whenScheduledAnnotationSpecifiesScheduler() {
        // given: there are two schedulers, so it is the qualifier of @Scheduled that decides, not the default

        new ApplicationContextRunner()
                .withPropertyValues(ENDPOINT_EXPOSURE_PROPERTY)
                .withUserConfiguration(QualifiedSchedulerConfig.class)
                .withConfiguration(AutoConfigurations.of(
                        TaskSchedulingAutoConfiguration.class,
                        TaskExecutionAutoConfiguration.class,
                        ScheduledTaskManagementAutoConfiguration.class))
                // when
                .run(context -> {
                    assertThat(context).doesNotHaveBean(TASK_SCHEDULER_BEAN_NAME);
                    assertThat(context)
                            .doesNotHaveBean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME);

                    BlockingQueue<String> executions = context.getBean(QualifiedSchedulerConfig.class).executionThreads;

                    assertReschedulesOnThread(context, executions, QUALIFIED_SCHEDULER_BEAN_NAME + "-");
                });
    }

    @Test // GH-1726
    void shouldExecuteOnApplicationScheduler_whenApplicationUsesSpringBootScheduler() {
        contextRunner.run(context -> assertExecutesOnThread(context, SPRING_BOOT_SCHEDULER_THREAD_PREFIX));
    }

    @Test // GH-1726
    void shouldExecuteOnApplicationScheduler_whenUserDeclaresTaskScheduler() {
        // given: the user declares own TaskScheduler

        contextRunner
                .withUserConfiguration(ThreadPoolTaskSchedulerConfig.class)
                // when
                .run(context -> assertExecutesOnThread(context, USER_TASK_SCHEDULER_BEAN_NAME + "-"));
    }

    @Test // GH-1726
    void shouldExecuteOnQualifiedScheduler_whenScheduledAnnotationSpecifiesScheduler() {
        // given: there are two schedulers, so it is the qualifier of @Scheduled that decides, not the default

        new ApplicationContextRunner()
                .withPropertyValues(ENDPOINT_EXPOSURE_PROPERTY)
                .withUserConfiguration(QualifiedSchedulerConfig.class)
                .withConfiguration(AutoConfigurations.of(
                        TaskSchedulingAutoConfiguration.class,
                        TaskExecutionAutoConfiguration.class,
                        ScheduledTaskManagementAutoConfiguration.class))
                // when
                .run(context -> {
                    BlockingQueue<String> executions = context.getBean(QualifiedSchedulerConfig.class).executionThreads;

                    assertExecutesOnThread(context, executions, QUALIFIED_SCHEDULER_BEAN_NAME + "-");
                });
    }

    @Test // GH-1497
    void shouldCancelAllManagedTasksWhenContextCloses() {
        contextRunner.run(context -> {
            ScheduledTasksRegistry registry = context.getBean(ScheduledTasksRegistry.class);

            Collection<ManagedScheduledTask> snapshot = new ArrayList<>(registry.getAll());

            assertThat(snapshot).isNotEmpty();
            for (ManagedScheduledTask task : snapshot) {
                assertThat(task.isEnabled()).isTrue();
            }

            context.close();

            for (ManagedScheduledTask task : snapshot) {
                assertThat(task.isEnabled()).isFalse();
            }
        });
    }

    @Test
    void shouldRegisterSeparateTaskForEveryRepeatableScheduledAnnotation() {
        contextRunner.withUserConfiguration(RepeatableSchedulingConfig.class).run(context -> {
            // then
            assertThat(context.getBean(TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME))
                    .isInstanceOf(AxelixScheduledAnnotationBeanPostProcessor.class);

            String jobs = RepeatableSchedulingConfig.class.getName();
            assertThat(context.getBean(ScheduledTasksRegistry.class).getAll())
                    .filteredOn(task -> task.getId().startsWith(jobs))
                    .extracting(
                            ManagedScheduledTask::getId, task -> task.getTask().getClass())
                    .containsExactlyInAnyOrder(
                            tuple(jobs + ".cronAndFixedDelay#cron0", CronTask.class),
                            tuple(jobs + ".cronAndFixedDelay#fixedDelay0", FixedDelayTask.class),
                            tuple(jobs + ".twoCrons#cron0", CronTask.class),
                            tuple(jobs + ".twoCrons#cron1", CronTask.class),
                            tuple(jobs + ".single#fixedRate0", FixedRateTask.class));

            assertThat(context.getBean(ScheduledTasksRegistry.class)
                            .findRequired(jobs + ".twoCrons#cron1")
                            .getTask())
                    .isInstanceOfSatisfying(
                            CronTask.class,
                            task -> assertThat(task.getExpression()).isEqualTo("0 0 3 * * *"));
        });
    }

    @Test
    void shouldDisableOnlyTheTaskOfTheGivenRepeatableScheduledAnnotation() {
        contextRunner.withUserConfiguration(RepeatableSchedulingConfig.class).run(context -> {
            String jobs = RepeatableSchedulingConfig.class.getName();
            ScheduledTasksRegistry registry = context.getBean(ScheduledTasksRegistry.class);

            // when
            context.getBean(ScheduledTaskService.class).disableTask(jobs + ".twoCrons#cron1", false);

            // then
            assertThat(registry.findRequired(jobs + ".twoCrons#cron1").isEnabled())
                    .isFalse();
            assertThat(registry.findRequired(jobs + ".twoCrons#cron0").isEnabled())
                    .isTrue();
        });
    }

    @Test
    void shouldKeepTheIdOfTheRepeatableScheduledAnnotationTaskWhenRescheduled() {
        contextRunner.withUserConfiguration(RepeatableSchedulingConfig.class).run(context -> {
            String jobs = RepeatableSchedulingConfig.class.getName();
            ScheduledTasksRegistry registry = context.getBean(ScheduledTasksRegistry.class);

            // when
            context.getBean(ScheduledTaskService.class).modifyCronExpression(jobs + ".twoCrons#cron1", "0 0 4 * * *");

            // then
            assertThat(registry.findRequired(jobs + ".twoCrons#cron1").getTask())
                    .isInstanceOfSatisfying(
                            CronTask.class,
                            task -> assertThat(task.getExpression()).isEqualTo("0 0 4 * * *"));
            assertThat(registry.findRequired(jobs + ".twoCrons#cron1").getRunnable())
                    .hasToString(jobs + ".twoCrons#cron1");
            assertThat(registry.findRequired(jobs + ".twoCrons#cron0").getTask())
                    .isInstanceOfSatisfying(
                            CronTask.class,
                            task -> assertThat(task.getExpression()).isEqualTo("0 0 2 * * *"));
        });
    }

    @Test
    void shouldRecordTheExecutionsOfScheduledTasks() {
        contextRunner.withUserConfiguration(RecordedSchedulingConfig.class).run(context -> {
            String jobs = RecordedSchedulingConfig.class.getName();

            // when: the tasks run right after the start

            // then
            assertThat(awaitExecutions(context, jobs + ".succeeding#fixedDelay0", 1))
                    .singleElement()
                    .satisfies(execution -> {
                        assertThat(execution.getSuccess()).isTrue();
                        assertThat(execution.getStartedAt()).isNotBlank();
                        assertThat(execution.getDurationMillis()).isNotNegative();
                        assertThat(execution.getErrorType()).isNull();
                        assertThat(execution.getErrorMessage()).isNull();
                    });
            assertThat(awaitExecutions(context, jobs + ".failing#fixedDelay0", 1))
                    .singleElement()
                    .satisfies(execution -> {
                        assertThat(execution.getSuccess()).isFalse();
                        assertThat(execution.getErrorType()).isEqualTo("IllegalStateException");
                        assertThat(execution.getErrorMessage()).isEqualTo("boom");
                    });
        });
    }

    @Test
    void shouldRecordTheExecutionsOfReactiveScheduledTasksWhenTheirPublishersComplete() {
        contextRunner
                .withConfiguration(AutoConfigurations.of(
                        ObservationAutoConfiguration.class, ScheduledTasksObservationAutoConfiguration.class))
                .withUserConfiguration(ReactiveSchedulingConfig.class)
                .run(ScheduledTaskManagementAutoConfigurationTest::assertRecordsTheReactiveExecutions);
    }

    @Test
    void shouldRecordTheExecutionsOfReactiveScheduledTasksWithoutTheObservationRegistryOfTheApplication() {
        contextRunner
                .withUserConfiguration(ReactiveSchedulingConfig.class)
                .run(ScheduledTaskManagementAutoConfigurationTest::assertRecordsTheReactiveExecutions);
    }

    @Test
    void shouldRecordTheExecutionOfOneTimeTask() {
        contextRunner.withUserConfiguration(RecordedSchedulingConfig.class).run(context -> {
            String jobs = RecordedSchedulingConfig.class.getName();

            // when: the task runs once right after the start

            // then
            assertThat(awaitExecutions(context, jobs + ".once#oneTime0", 1))
                    .singleElement()
                    .satisfies(execution -> assertThat(execution.getSuccess()).isTrue());
        });
    }

    @Test
    void shouldRecordTheExecutionsOfEveryRepeatableScheduledAnnotationOnce() {
        contextRunner.withUserConfiguration(RepeatableSchedulingConfig.class).run(context -> {
            String jobs = RepeatableSchedulingConfig.class.getName();

            // when: the interval tasks run right after the start, the cron ones do not run at all

            // then
            assertThat(awaitExecutions(context, jobs + ".cronAndFixedDelay#fixedDelay0", 1))
                    .hasSize(1);
            assertThat(awaitExecutions(context, jobs + ".single#fixedRate0", 1)).hasSize(1);
            assertThat(executionsOf(context, jobs + ".cronAndFixedDelay#cron0")).isEmpty();
        });
    }

    @Test
    void shouldRecordTheManualExecutionOfScheduledTask() {
        contextRunner.withUserConfiguration(RecordedSchedulingConfig.class).run(context -> {
            String taskId = RecordedSchedulingConfig.class.getName() + ".succeeding#fixedDelay0";
            ScheduledTaskService service = context.getBean(ScheduledTaskService.class);
            assertThat(awaitExecutions(context, taskId, 1)).hasSize(1);
            service.disableTask(taskId, false);

            // when
            service.executeScheduledTask(taskId);

            // then
            assertThat(awaitExecutions(context, taskId, 2)).hasSize(2);
        });
    }

    @Test
    void shouldNotRecordTheExecutionsOfTasksRegisteredViaSchedulingConfigurer() {
        contextRunner.withUserConfiguration(SchedulingConfigurerConfig.class).run(context -> {
            BlockingQueue<String> executions = context.getBean(SchedulingConfigurerConfig.class).executions;
            String taskId = context.getBean(ScheduledTasksRegistry.class).getAll().stream()
                    .map(ManagedScheduledTask::getId)
                    .filter(id -> !id.startsWith(EnableSchedulingConfig.class.getName()))
                    .findFirst()
                    .orElseThrow();
            assertThat(executions.poll(5, TimeUnit.SECONDS)).isNotNull();

            // when
            context.getBean(ScheduledTaskService.class).executeScheduledTask(taskId);

            // then: the task is still run manually, but none of its executions is recorded
            assertThat(executions.poll(5, TimeUnit.SECONDS)).isNotNull();
            Thread.sleep(200);
            assertThat(context.getBean(ScheduledTaskExecutionHistory.class).getExecutions())
                    .extracting(ScheduledTaskExecution::getTaskId)
                    .doesNotContain(taskId);
        });
    }

    @Test
    void shouldNotReplaceTheScheduledAnnotationProcessorDeclaredByUser() {
        new ApplicationContextRunner()
                .withPropertyValues(ENDPOINT_EXPOSURE_PROPERTY)
                .withUserConfiguration(UserScheduledAnnotationProcessorConfig.class)
                .withConfiguration(AutoConfigurations.of(ScheduledTaskManagementAutoConfiguration.class))
                .run(context -> {
                    // then
                    assertThat(context.getBean(TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME))
                            .isNotInstanceOf(AxelixScheduledAnnotationBeanPostProcessor.class);

                    String taskId = UserScheduledAnnotationProcessorConfig.class.getName() + ".task";
                    assertThat(context.getBean(ScheduledTasksRegistry.class).getAll())
                            .extracting(ManagedScheduledTask::getId)
                            .containsExactly(taskId);

                    // the task is still managed, but its executions are not recorded
                    BlockingQueue<String> executions =
                            context.getBean(UserScheduledAnnotationProcessorConfig.class).executions;
                    assertThat(executions.poll(5, TimeUnit.SECONDS)).isNotNull();
                    context.getBean(ScheduledTaskService.class).disableTask(taskId, false);
                    assertThat(context.getBean(ScheduledTasksRegistry.class)
                                    .findRequired(taskId)
                                    .isEnabled())
                            .isFalse();
                    assertThat(context.getBean(ScheduledTaskExecutionHistory.class)
                                    .getExecutions())
                            .isEmpty();
                });
    }

    private static void assertReschedulesOnThread(AssertableApplicationContext context, String threadNamePrefix)
            throws InterruptedException {
        assertReschedulesOnThread(
                context, context.getBean(EnableSchedulingConfig.class).executionThreads, threadNamePrefix);
    }

    private static void assertReschedulesOnThread(
            AssertableApplicationContext context, BlockingQueue<String> executions, String threadNamePrefix)
            throws InterruptedException {
        ScheduledTaskService service = context.getBean(ScheduledTaskService.class);
        ManagedScheduledTask task = context.getBean(ScheduledTasksRegistry.class)
                .getAll()
                .iterator()
                .next();

        // the initial run takes place on the scheduler of the application
        assertThat(executions.poll(5, TimeUnit.SECONDS)).startsWith(threadNamePrefix);

        service.modifyInterval(task.getId(), Duration.ofMillis(50));

        // the re-scheduled task keeps running on the very same scheduler
        assertThat(executions.poll(5, TimeUnit.SECONDS)).startsWith(threadNamePrefix);
    }

    private static void assertExecutesOnThread(AssertableApplicationContext context, String threadNamePrefix)
            throws InterruptedException {
        assertExecutesOnThread(
                context, context.getBean(EnableSchedulingConfig.class).executionThreads, threadNamePrefix);
    }

    private static void assertExecutesOnThread(
            AssertableApplicationContext context, BlockingQueue<String> executions, String threadNamePrefix)
            throws InterruptedException {
        ScheduledTaskService service = context.getBean(ScheduledTaskService.class);
        ManagedScheduledTask task = context.getBean(ScheduledTasksRegistry.class)
                .getAll()
                .iterator()
                .next();

        // the initial run takes place on the scheduler of the application
        assertThat(executions.poll(5, TimeUnit.SECONDS)).startsWith(threadNamePrefix);

        service.executeScheduledTask(task.getId());

        // the manual run takes place on the very same scheduler
        assertThat(executions.poll(5, TimeUnit.SECONDS)).startsWith(threadNamePrefix);
    }

    private static void assertRecordsTheReactiveExecutions(AssertableApplicationContext context)
            throws InterruptedException {
        String jobs = ReactiveSchedulingConfig.class.getName();

        // when: the tasks run right after the start

        // then
        assertThat(awaitExecutions(context, jobs + ".succeeding#fixedRate0", 1))
                .singleElement()
                .satisfies(execution -> {
                    assertThat(execution.getSuccess()).isTrue();
                    assertThat(execution.getDurationMillis())
                            .isGreaterThanOrEqualTo(ReactiveSchedulingConfig.DELAY_MILLIS);
                });
        assertThat(awaitExecutions(context, jobs + ".failing#fixedRate0", 1))
                .singleElement()
                .satisfies(execution -> {
                    assertThat(execution.getSuccess()).isFalse();
                    assertThat(execution.getErrorType()).isEqualTo("IllegalStateException");
                    assertThat(execution.getErrorMessage()).isEqualTo("reactive boom");
                    assertThat(execution.getDurationMillis())
                            .isGreaterThanOrEqualTo(ReactiveSchedulingConfig.DELAY_MILLIS);
                });
    }

    private static List<ScheduledTaskExecution> awaitExecutions(
            AssertableApplicationContext context, String taskId, int count) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        List<ScheduledTaskExecution> executions = executionsOf(context, taskId);
        while (executions.size() < count && System.nanoTime() < deadline) {
            Thread.sleep(10);
            executions = executionsOf(context, taskId);
        }
        return executions;
    }

    private static List<ScheduledTaskExecution> executionsOf(AssertableApplicationContext context, String taskId) {
        return context.getBean(ScheduledTaskExecutionHistory.class).getExecutions().stream()
                .filter(execution -> execution.getTaskId().equals(taskId))
                .toList();
    }

    @TestConfiguration
    @EnableScheduling
    static class EnableSchedulingConfig {

        final BlockingQueue<String> executionThreads = new LinkedBlockingQueue<>();

        // simulation of schedule tasks
        @Scheduled(fixedDelay = 10000)
        public void someMockTask() {
            executionThreads.add(Thread.currentThread().getName());
        }
    }

    @TestConfiguration
    static class RepeatableSchedulingConfig {

        @Scheduled(cron = "0 0 1 * * *")
        @Scheduled(fixedDelay = 100000)
        public void cronAndFixedDelay() {}

        @Scheduled(cron = "0 0 2 * * *")
        @Scheduled(cron = "0 0 3 * * *")
        public void twoCrons() {}

        @Scheduled(fixedRate = 100000)
        public void single() {}
    }

    @TestConfiguration
    static class RecordedSchedulingConfig {

        @Scheduled(fixedDelay = 100000)
        public void succeeding() {}

        @Scheduled(fixedDelay = 100000)
        public void failing() {
            throw new IllegalStateException("boom");
        }

        @Scheduled(initialDelay = 0)
        public void once() {}
    }

    @TestConfiguration
    static class ReactiveSchedulingConfig {

        static final long DELAY_MILLIS = 200;

        @Scheduled(fixedRate = 100000)
        public Mono<Void> succeeding() {
            return Mono.delay(Duration.ofMillis(DELAY_MILLIS)).then();
        }

        @Scheduled(fixedRate = 100000)
        public Mono<Void> failing() {
            return Mono.delay(Duration.ofMillis(DELAY_MILLIS))
                    .then(Mono.error(new IllegalStateException("reactive boom")));
        }
    }

    @TestConfiguration
    static class SchedulingConfigurerConfig implements SchedulingConfigurer {

        final BlockingQueue<String> executions = new LinkedBlockingQueue<>();

        @Override
        public void configureTasks(ScheduledTaskRegistrar registrar) {
            registrar.addFixedDelayTask(() -> executions.add("executed"), Duration.ofSeconds(100));
        }
    }

    @TestConfiguration
    static class UserScheduledAnnotationProcessorConfig {

        final BlockingQueue<String> executions = new LinkedBlockingQueue<>();

        @Bean(TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)
        static ScheduledAnnotationBeanPostProcessor scheduledAnnotationProcessor() {
            return new ScheduledAnnotationBeanPostProcessor();
        }

        @Scheduled(fixedDelay = 100000)
        public void task() {
            executions.add("executed");
        }
    }

    @TestConfiguration
    static class ThreadPoolTaskSchedulerConfig {

        @Bean
        public ThreadPoolTaskScheduler userTaskScheduler() {
            return new ThreadPoolTaskScheduler();
        }
    }

    @TestConfiguration
    @EnableScheduling
    static class QualifiedSchedulerConfig {

        final BlockingQueue<String> executionThreads = new LinkedBlockingQueue<>();

        @Bean
        public ThreadPoolTaskScheduler qualifiedScheduler() {
            return new ThreadPoolTaskScheduler();
        }

        @Bean
        public ThreadPoolTaskScheduler otherScheduler() {
            return new ThreadPoolTaskScheduler();
        }

        // simulation of a scheduled task that is bound to the particular scheduler
        @Scheduled(scheduler = QUALIFIED_SCHEDULER_BEAN_NAME, fixedDelay = 10000)
        public void qualifiedMockTask() {
            executionThreads.add(Thread.currentThread().getName());
        }
    }
}
