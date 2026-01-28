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
package com.axelixlabs.axelix.sbs.spring.threaddump;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ThreadDumpManagementEndpoint}
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ThreadDumpManagementEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldEnableThreadContentionMonitoring() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

        // when.
        restTemplate.postForEntity("/actuator/axelix-thread-dump/enable", null, Void.class);

        // then.
        assertThat(threadMXBean.isThreadContentionMonitoringEnabled()).isTrue();
    }

    @Test
    void shouldDisableThreadContentionMonitoring() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        threadMXBean.setThreadContentionMonitoringEnabled(true);

        // when.
        restTemplate.postForEntity("/actuator/axelix-thread-dump/disable", null, Void.class);

        // then.
        assertThat(threadMXBean.isThreadContentionMonitoringEnabled()).isFalse();
    }

    @TestConfiguration
    static class ThreadDumpManagementEndpointTestConfiguration {
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
