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
package com.axelixlabs.axelix.master.mcp.tools;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LogsMcpServerTools}.
 *
 * @author Mikhail Polivakha
 */
class LogsMcpServerToolsTest {

    private static final String INSTANCE_ID = "instance-1";

    private final EndpointInvoker endpointInvoker = mock(EndpointInvoker.class);

    private final LogsMcpServerTools subject = new LogsMcpServerTools(endpointInvoker);

    @Test
    void shouldSendTheLimitAsTheQueryParameter() {
        // given.
        String logEntries = "{\"entries\":[]}";
        when(endpointInvoker.invoke(eq(InstanceId.of(INSTANCE_ID)), eq(ActuatorEndpoints.GET_LOG_ENTRIES_TAIL), any()))
                .thenReturn(logEntries.getBytes(StandardCharsets.UTF_8));

        // when.
        String result = subject.getInstanceLogs(INSTANCE_ID, 25);

        // then.
        ArgumentCaptor<HttpPayload> payloadCaptor = ArgumentCaptor.forClass(HttpPayload.class);
        verify(endpointInvoker)
                .invoke(
                        eq(InstanceId.of(INSTANCE_ID)),
                        eq(ActuatorEndpoints.GET_LOG_ENTRIES_TAIL),
                        payloadCaptor.capture());

        assertThat(result).isEqualTo(logEntries);
        assertThat(payloadCaptor.getValue().queryParameters())
                .singleElement()
                .satisfies(parameter -> assertThat(parameter.toEncodedString()).isEqualTo("limit=25"));
    }
}
