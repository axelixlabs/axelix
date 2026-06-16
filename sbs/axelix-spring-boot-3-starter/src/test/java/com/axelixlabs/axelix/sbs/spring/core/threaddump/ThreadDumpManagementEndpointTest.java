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
package com.axelixlabs.axelix.sbs.spring.core.threaddump;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.sbs.spring.core.utils.auth.ProtectedEndpointTests;
import com.axelixlabs.axelix.sbs.spring.shared.AbstractEndpointIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ThreadDumpManagementEndpoint}
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public class ThreadDumpManagementEndpointTest extends AbstractEndpointIntegrationTest {

    @Test
    void shouldEnableThreadContentionMonitoring() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        // when.
        testRestTemplate.asViewer().postForEntity("/actuator/axelix-thread-dump/enable", null, Void.class);

        // then.
        assertThat(threadMXBean.isThreadContentionMonitoringEnabled()).isTrue();
    }

    @Test
    void shouldReceiveThreadDumpJson() {
        // when.
        ResponseEntity<String> response =
                testRestTemplate.asViewer().getForEntity("/actuator/axelix-thread-dump", String.class);

        // then.
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void shouldDisableThreadContentionMonitoring() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        threadMXBean.setThreadContentionMonitoringEnabled(true);

        // when.
        testRestTemplate.asViewer().postForEntity("/actuator/axelix-thread-dump/disable", null, Void.class);

        // then.
        assertThat(threadMXBean.isThreadContentionMonitoringEnabled()).isFalse();
    }

    @ProtectedEndpointTests(method = HttpMethod.GET, path = "/actuator/axelix-thread-dump")
    void negativeAuthTests() {}

    @TestConfiguration
    public static class ThreadDumpManagementEndpointTestConfiguration {

        @Bean
        public ThreadDumpContentionMonitoringManagement management() {
            return new DefaultThreadDumpContentionMonitoringManagement();
        }

        @Bean
        public ThreadDumpManagementEndpoint threadDumpManagementEndpoint(
                ThreadDumpContentionMonitoringManagement management) {
            return new ThreadDumpManagementEndpoint(management);
        }
    }
}
