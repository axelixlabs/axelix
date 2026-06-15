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
package com.axelixlabs.axelix.sbs.spring.core.loggers;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.logging.LoggerGroups;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Test configuration for {@link AxelixLoggersEndpointTest}, part of the shared endpoint test context.
 *
 * @author Sergey Cherkasov
 */
@TestConfiguration
public class LoggersEndpointTestConfiguration {

    @Bean
    public AxelixLoggersEndpoint axelixLoggersEndpoint(
            LoggingSystem loggingSystem, ObjectProvider<LoggerGroups> loggerGroups) {
        return new AxelixLoggersEndpoint(loggingSystem, loggerGroups.getIfAvailable(LoggerGroups::new));
    }
}
