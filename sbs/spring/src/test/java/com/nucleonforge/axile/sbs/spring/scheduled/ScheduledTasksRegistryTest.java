package com.nucleonforge.axile.sbs.spring.scheduled;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.FixedDelayTask;
import org.springframework.scheduling.config.FixedRateTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TriggerTask;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ScheduledTasksRegistry}
 *
 * @since 14.10.2025
 * @author Nikita Kirillov
 */
@SpringBootTest
@Import(ScheduledTasksRegistryTest.ScheduledTaskRegistryTestConfiguration.class)
class ScheduledTasksRegistryTest {

    private static final String CRON_TASK_ID =
            "com.nucleonforge.axile.sbs.spring.scheduled.ScheduledTasksRegistryTest$ScheduledTaskRegistryTestConfiguration.testCronTask";
    private static final String FIXED_DELAY_TASK_ID =
            "com.nucleonforge.axile.sbs.spring.scheduled.ScheduledTasksRegistryTest$ScheduledTaskRegistryTestConfiguration.testFixedDelayTask";
    private static final String FIXED_RATE_TASK_ID =
            "com.nucleonforge.axile.sbs.spring.scheduled.ScheduledTasksRegistryTest$ScheduledTaskRegistryTestConfiguration.testFixedRateTask";
    private static final String CUSTOM_TASK_ID =
            "com.nucleonforge.axile.sbs.spring.scheduled.ScheduledTaskRegistryTest$ScheduledTaskRegistryTestConfiguration.testCustomTask";

    @Autowired
    private ScheduledTasksRegistry taskRegistry;

    @Test
    void shouldRegisterAllScheduledTasks() {
        Collection<ManagedScheduledTask> registeredTasks = taskRegistry.getAll();

        assertThat(registeredTasks).isNotNull().isNotEmpty().hasSize(4);

        List<String> taskIds =
                registeredTasks.stream().map(ManagedScheduledTask::getId).toList();

        assertThat(taskIds)
                .hasSize(4)
                .allSatisfy(id -> assertThat(id).isNotBlank())
                .containsExactlyInAnyOrder(CRON_TASK_ID, FIXED_DELAY_TASK_ID, FIXED_RATE_TASK_ID, CUSTOM_TASK_ID);

        assertThat(registeredTasks)
                .allSatisfy(task -> assertThat(task.getFuture().isCancelled()).isFalse());

        taskIds.forEach(id -> assertThat(taskRegistry.find(id))
                .isPresent()
                .get()
                .extracting(ManagedScheduledTask::getId)
                .isEqualTo(id));

        assertThat(taskRegistry.find("non-existent-task")).isEmpty();
    }

    @Test
    void shouldHaveCorrectTaskTypes() {
        Collection<ManagedScheduledTask> tasks = taskRegistry.getAll();

        assertThat(tasks)
                .hasSize(4)
                .extracting(ManagedScheduledTask::getScheduledTask)
                .extracting(ScheduledTask::getTask)
                .satisfiesExactlyInAnyOrder(
                        task -> assertThat(task).isInstanceOf(CronTask.class),
                        task -> assertThat(task).isInstanceOf(FixedDelayTask.class),
                        task -> assertThat(task).isInstanceOf(FixedRateTask.class),
                        task -> assertThat(task).isInstanceOf(TriggerTask.class).isNotInstanceOf(CronTask.class));
    }

    @TestConfiguration
    @EnableScheduling
    static class ScheduledTaskRegistryTestConfiguration implements SchedulingConfigurer {

        @Bean
        public TaskScheduler taskScheduler() {
            return new ConcurrentTaskScheduler();
        }

        @Bean
        public ScheduledTasksRegistry scheduledTaskRegistry(
                ScheduledAnnotationBeanPostProcessor processor, TaskScheduler scheduler) {
            return new ScheduledTasksRegistry(processor, scheduler);
        }

        @Scheduled(cron = "*/2 * * * * *")
        public void testCronTask() {}

        @Scheduled(fixedDelay = 2000)
        public void testFixedDelayTask() {}

        @Scheduled(fixedRate = 2000, initialDelay = 100)
        public void testFixedRateTask() {}

        @Override
        public void configureTasks(ScheduledTaskRegistrar registrar) {
            Runnable customTask = new Runnable() {
                @Override
                public void run() {
                    executeCustomTask();
                }

                @Override
                public String toString() {
                    return CUSTOM_TASK_ID;
                }
            };
            registrar.addTriggerTask(customTask, new CustomTrigger());
        }

        private void executeCustomTask() {}

        static class CustomTrigger implements Trigger {
            @Override
            @Nullable
            public Instant nextExecution(@NonNull TriggerContext triggerContext) {
                return Instant.now().plusMillis(1000);
            }
        }
    }
}
