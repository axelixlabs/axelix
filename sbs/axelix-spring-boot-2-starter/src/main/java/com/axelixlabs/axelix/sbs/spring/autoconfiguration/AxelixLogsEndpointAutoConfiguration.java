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

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.axelixlabs.axelix.sbs.spring.core.SlidingWindow;
import com.axelixlabs.axelix.sbs.spring.core.contract.logs.LogEntry;
import com.axelixlabs.axelix.sbs.spring.core.logs.AxelixLogsEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.logs.Log4j2InMemoryLogsAppender;
import com.axelixlabs.axelix.sbs.spring.core.logs.LogbackInMemoryLogsAppender;
import com.axelixlabs.axelix.sbs.spring.core.persistence.hibernate.ConditionalOnLoggingSystem;

/**
 * Auto-configuration for the {@link AxelixLogsEndpoint}.
 *
 * @author Mikhail Polivakha
 */
@AxelixAutoConfiguration
@ConditionalOnAvailableEndpoint(endpoint = AxelixLogsEndpoint.class)
public class AxelixLogsEndpointAutoConfiguration {

    /**
     * The amount of the most recent log entries kept in memory.
     */
    private static final int LOG_ENTRIES_CAPACITY = 1000;

    @Bean
    public SlidingWindow<LogEntry> logEntries() {
        return new SlidingWindow<>(LOG_ENTRIES_CAPACITY);
    }

    @Bean
    public AxelixLogsEndpoint axelixLogsEndpoint(SlidingWindow<LogEntry> logEntries) {
        return new AxelixLogsEndpoint(logEntries);
    }

    @Configuration
    @ConditionalOnLoggingSystem(ConditionalOnLoggingSystem.System.LOGBACK)
    static class LogbackInMemoryLogsAppenderConfiguration {

        // Registered right away, and not on ApplicationReadyEvent, to capture the startup logs as well.
        LogbackInMemoryLogsAppenderConfiguration(SlidingWindow<LogEntry> logEntries) {
            LogbackInMemoryLogsAppender.register(logEntries);
        }
    }

    @Configuration
    @ConditionalOnLoggingSystem(ConditionalOnLoggingSystem.System.LOG4J2)
    static class Log4j2InMemoryLogsAppenderConfiguration {

        // Registered right away, and not on ApplicationReadyEvent, to capture the startup logs as well.
        Log4j2InMemoryLogsAppenderConfiguration(SlidingWindow<LogEntry> logEntries) {
            Log4j2InMemoryLogsAppender.register(logEntries);
        }
    }
}
