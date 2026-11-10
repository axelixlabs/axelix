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
package com.axelixlabs.axelix.master.service.scheduled;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import tools.jackson.databind.json.JsonMapper;

import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.domain.instance.Instance;
import com.axelixlabs.axelix.master.domain.instance.InstanceId;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultScheduledTasksServiceTest {

    @ParameterizedTest
    @CsvSource({
        "com.example.Jobs.run,com.example.Jobs,run",
        "com.example.Jobs$Nested.run#cron0,com.example.Jobs$Nested,run",
        "com.example.Jobs.run#fixedDelay0,com.example.Jobs,run",
        "com.example.Jobs.run#fixedRate12,com.example.Jobs,run",
        "com.example.Jobs$$Lambda$1/0x123@abc,com.example.Jobs,lambda",
        "com.example.Jobs$$Lambda/0x123@abc,com.example.Jobs,lambda",
        "com.example.Jobs$Nested$$Lambda$12@abc,com.example.Jobs$Nested,lambda",
        "com.example.Jobs$$Lambda/123@abc,com.example.Jobs,lambda",
        "com.example.Jobs@abc,com.example.Jobs,unknown",
        "com.example.Jobs$$Lambda$bad/0x123@abc,unknown,unknown",
        "unknown,unknown,unknown"
    })
    void infersMissingMetadata(String taskId, String className, String methodName) {
        // given.
        DefaultScheduledTasksService service = service("{\"target\":\"" + taskId + "\"}");

        // when.
        var task = service.getScheduledTasks(InstanceId.of("test")).cron().getFirst();

        // then.
        assertThat(task.className()).isEqualTo(className);
        assertThat(task.methodName()).isEqualTo(methodName);
        assertThat(task.taskId()).isEqualTo(taskId);
    }

    @ParameterizedTest
    @CsvSource({"opaque,com.example.ActualJobs,execute", "com.example.OldJobs.run,com.example.ActualJobs,execute"})
    void prefersExplicitMetadata(String taskId, String className, String methodName) {
        // given.
        DefaultScheduledTasksService service = service("{\"target\":\"" + taskId + "\",\"className\":\"" + className
                + "\",\"methodName\":\"" + methodName + "\"}");

        // when.
        var task = service.getScheduledTasks(InstanceId.of("test")).cron().getFirst();

        // then.
        assertThat(task.className()).isEqualTo(className);
        assertThat(task.methodName()).isEqualTo(methodName);
    }

    private DefaultScheduledTasksService service(String runnable) {
        EndpointInvoker invoker = mock(EndpointInvoker.class);
        InstanceRegistry registry = mock(InstanceRegistry.class);
        Instance instance = mock(Instance.class);
        ScheduledTaskExecutionHistoryService history = mock(ScheduledTaskExecutionHistoryService.class);
        when(registry.getOrThrow(any())).thenReturn(instance);
        when(history.findSuccessRates(any(), any())).thenReturn(Map.of());
        String body = "{\"cron\":[{\"runnable\":" + runnable
                + ",\"expression\":\"* * * * * *\",\"enabled\":true}],\"fixedDelay\":[],\"fixedRate\":[],\"custom\":[]}";
        when(invoker.invoke(InstanceId.of("test"), ActuatorEndpoints.GET_SCHEDULED_TASKS, NoHttpPayload.INSTANCE))
                .thenReturn(body.getBytes(StandardCharsets.UTF_8));
        return new DefaultScheduledTasksService(invoker, JsonMapper.builder().build(), registry, history);
    }
}
