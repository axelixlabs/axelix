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
package com.axelixlabs.axelix.master.service.convert;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.ServiceScheduledTasks;
import com.axelixlabs.axelix.master.api.external.response.ScheduledTasksResponse;
import com.axelixlabs.axelix.master.service.convert.response.ServiceScheduledTasksConverter;

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
        assertThat(custom.nextExecution().time()).isEqualTo("2025-09-18T17:59:59.999098218Z");
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
        ServiceScheduledTasks.CronTask cronTask =
                new ServiceScheduledTasks.CronTask(runnable, "0 0 0/3 1/1 * ?", nextExecution, lastExecution, true);

        // FixedRate
        ServiceScheduledTasks.FixedRateTask fixedRateTask =
                new ServiceScheduledTasks.FixedRateTask(runnable, 3000, 10000, nextExecution, lastExecution, true);

        // FixedDelay
        ServiceScheduledTasks.FixedDelayTask fixedDelayTask =
                new ServiceScheduledTasks.FixedDelayTask(runnable, null, null, null, null, false);

        // Custom
        ServiceScheduledTasks.CustomTask customTask = new ServiceScheduledTasks.CustomTask(
                runnable,
                "com.example.Processor$CustomTrigger@56567e9b",
                nextExecution,
                lastExecutionEmptyException,
                false);

        // ServiceScheduledTasks -> return
        return new ServiceScheduledTasks(
                List.of(cronTask),
                List.of(fixedDelayTask, fixedDelayTask),
                List.of(fixedRateTask),
                List.of(customTask));
    }
}
