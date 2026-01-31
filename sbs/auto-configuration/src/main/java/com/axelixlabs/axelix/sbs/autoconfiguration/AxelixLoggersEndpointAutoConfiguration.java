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
package com.axelixlabs.axelix.sbs.autoconfiguration;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.boot.logging.LoggerGroups;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.sbs.spring.loggers.AxelixLoggersEndpoint;

/**
 * Auto-configuration for the {@link AxelixLoggersEndpoint}.
 *
 * @author Sergey Cherkasov
 */
@AutoConfiguration
public class AxelixLoggersEndpointAutoConfiguration {

    /**
     * {@link LoggingSystem} and {@link LoggerGroups} beans are registered dynamically in {@link LoggingApplicationListener}.
     */
    @Bean
    @ConditionalOnMissingBean
    public AxelixLoggersEndpoint axelixLoggersEndpoint(
            LoggingSystem loggingSystem, ObjectProvider<LoggerGroups> loggerGroups) {
        return new AxelixLoggersEndpoint(
                new LoggersEndpoint(loggingSystem, loggerGroups.getIfAvailable(LoggerGroups::new)));
    }
}
