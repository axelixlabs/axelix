package com.nucleonforge.axile.master.service.convert;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.ServiceScheduledTasks;
import com.nucleonforge.axile.master.api.response.ScheduledTasksResponse;
import com.nucleonforge.axile.master.service.convert.response.ServiceScheduledTasksConverter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ServiceScheduledTasksConverter}
 *
 * @author Sergey Cherkasov
 */
public class ServiceScheduledTasksConverterTest {

    private final ServiceScheduledTasksConverter subject = new ServiceScheduledTasksConverter();

    @Test
    void testConvertHappyPath() {
        // when.
        ScheduledTasksResponse response = subject.convertInternal(getServiceScheduledTasks());

        // Cron
        ScheduledTasksResponse.Cron cron = response.cron().get(0);
        assertThat(cron.enabled()).isTrue();
        assertThat(cron.runnable().target()).isEqualTo("com.example.Processor.processOrders");
        assertThat(cron.expression()).isEqualTo("0 0 0/3 1/1 * ?");
        assertThat(cron.nextExecution().time()).isEqualTo("2025-09-18T17:59:59.999098218Z");
        assertThat(cron.nextExecution().time()).isEqualTo("2025-09-18T17:59:59.999098218Z");
        assertThat(cron.lastExecution().status()).isEqualTo("ERROR");
        assertThat(cron.lastExecution().time()).isEqualTo("2025-09-18T15:03:34.132500256Z");
        assertThat(cron.lastExecution().exception().type()).isEqualTo("java.lang.IllegalStateException");
        assertThat(cron.lastExecution().exception().message()).isEqualTo("Failed while running custom task");

        // FixedRate
        ScheduledTasksResponse.FixedRate fixedRate = response.fixedRate().get(0);
        assertThat(fixedRate.enabled()).isTrue();
        assertThat(fixedRate.runnable().target()).isEqualTo("com.example.Processor.processOrders");
        assertThat(fixedRate.interval()).isEqualTo(3000);
        assertThat(fixedRate.initialDelay()).isEqualTo(10000);
        assertThat(fixedRate.nextExecution().time()).isEqualTo("2025-09-18T17:59:59.999098218Z");
        assertThat(fixedRate.lastExecution().status()).isEqualTo("ERROR");
        assertThat(fixedRate.lastExecution().time()).isEqualTo("2025-09-18T15:03:34.132500256Z");
        assertThat(fixedRate.lastExecution().exception().type()).isEqualTo("java.lang.IllegalStateException");
        assertThat(fixedRate.lastExecution().exception().message()).isEqualTo("Failed while running custom task");

        // FixedDelay1
        ScheduledTasksResponse.FixedDelay fixedDelay1 = response.fixedDelay().get(0);
        assertThat(fixedDelay1.enabled()).isFalse();
        assertThat(fixedDelay1.runnable().target()).isEqualTo("com.example.Processor.processOrders");
        assertThat(fixedDelay1.interval()).isNull();
        assertThat(fixedDelay1.initialDelay()).isNull();
        assertThat(fixedDelay1.nextExecution()).isNull();
        assertThat(fixedDelay1.lastExecution()).isNull();

        // FixedDelay2
        ScheduledTasksResponse.FixedDelay fixedDelay2 = response.fixedDelay().get(1);
        assertThat(fixedDelay2.enabled()).isFalse();
        assertThat(fixedDelay2.runnable().target()).isEqualTo("com.example.Processor.processOrders");
        assertThat(fixedDelay2.interval()).isNull();
        assertThat(fixedDelay2.initialDelay()).isNull();
        assertThat(fixedDelay2.nextExecution()).isNull();
        assertThat(fixedDelay2.lastExecution()).isNull();

        // Custom
        ScheduledTasksResponse.Custom custom = response.custom().get(0);
        assertThat(custom.enabled()).isFalse();
        assertThat(custom.runnable().target()).isEqualTo("com.example.Processor.processOrders");
        assertThat(custom.trigger()).isEqualTo("com.example.Processor$CustomTrigger@56567e9b");
        assertThat(custom.lastExecution().status()).isEqualTo("ERROR");
        assertThat(custom.lastExecution().time()).isEqualTo("2025-09-18T15:03:34.132500256Z");
        assertThat(custom.lastExecution().exception()).isNull();
    }

    private ServiceScheduledTasks getServiceScheduledTasks() {
        // Runnable
        ServiceScheduledTasks.Runnable runnable =
                new ServiceScheduledTasks.Runnable("com.example.Processor.processOrders");

        // NextExecution
        ServiceScheduledTasks.NextExecution nextExecution =
                new ServiceScheduledTasks.NextExecution("2025-09-18T17:59:59.999098218Z");

        // Exception
        ServiceScheduledTasks.LastExecution.Exception exception = new ServiceScheduledTasks.LastExecution.Exception(
                "java.lang.IllegalStateException", "Failed while running custom task");

        // LastExecution -> lastExecution
        ServiceScheduledTasks.LastExecution lastExecution =
                new ServiceScheduledTasks.LastExecution("ERROR", "2025-09-18T15:03:34.132500256Z", exception);

        // LastExecution -> lastExecutionEmptyException
        ServiceScheduledTasks.LastExecution lastExecutionEmptyException =
                new ServiceScheduledTasks.LastExecution("ERROR", "2025-09-18T15:03:34.132500256Z", null);

        // Cron
        ServiceScheduledTasks.CronTask.Cron cron =
                new ServiceScheduledTasks.CronTask.Cron(runnable, "0 0 0/3 1/1 * ?", nextExecution, lastExecution);

        // CronTask
        ServiceScheduledTasks.CronTask cronTask = new ServiceScheduledTasks.CronTask(cron, true);

        // FixedRate
        ServiceScheduledTasks.FixedRateTask.FixedRate fixedRate =
                new ServiceScheduledTasks.FixedRateTask.FixedRate(runnable, 3000, 10000, nextExecution, lastExecution);

        // FixedRateTask
        ServiceScheduledTasks.FixedRateTask fixedRateTask = new ServiceScheduledTasks.FixedRateTask(fixedRate, true);

        // FixedDelay
        ServiceScheduledTasks.FixedDelayTask.FixedDelay fixedDelay =
                new ServiceScheduledTasks.FixedDelayTask.FixedDelay(runnable, null, null, null, null);

        // FixedDelayTask
        ServiceScheduledTasks.FixedDelayTask fixedDelayTask =
                new ServiceScheduledTasks.FixedDelayTask(fixedDelay, false);

        // Custom
        ServiceScheduledTasks.CustomTask.Custom custom = new ServiceScheduledTasks.CustomTask.Custom(
                runnable, "com.example.Processor$CustomTrigger@56567e9b", lastExecutionEmptyException);

        // CustomTask
        ServiceScheduledTasks.CustomTask customTask = new ServiceScheduledTasks.CustomTask(custom, false);

        // ServiceScheduledTasks -> return
        return new ServiceScheduledTasks(
                List.of(cronTask),
                List.of(fixedDelayTask, fixedDelayTask),
                List.of(fixedRateTask),
                List.of(customTask));
    }
}
