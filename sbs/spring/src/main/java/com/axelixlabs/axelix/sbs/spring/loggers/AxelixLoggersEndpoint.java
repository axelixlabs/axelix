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
package com.axelixlabs.axelix.sbs.spring.loggers;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.actuate.logging.LoggersEndpoint.LoggerLevelsDescriptor;
import org.springframework.boot.actuate.logging.LoggersEndpoint.LoggersDescriptor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.lang.Nullable;

/**
 * Custom Spring Boot Actuator endpoint exposing the application's loggers.
 *
 * @author Sergey Cherkasov
 */
@Endpoint(id = "axelix-loggers")
public class AxelixLoggersEndpoint {

    private final LoggersEndpoint delegate;

    public AxelixLoggersEndpoint(LoggersEndpoint delegate) {
        this.delegate = delegate;
    }

    @ReadOperation
    public LoggersDescriptor loggers() {
        return delegate.loggers();
    }

    @ReadOperation
    public LoggerLevelsDescriptor loggerLevels(@Selector String name) {
        return delegate.loggerLevels(name);
    }

    // IMPORTANT!
    // For Spring Actuator endpoints @Endpoint, we must use org.springframework.lang.Nullable.
    // Spring Boot 3 does not recognize the Jspecify's @Nullable here, but we still need to tell
    // Spring that tags are optional
    @WriteOperation
    public void configureLogLevel(@Selector String name, @Nullable LogLevel configuredLevel) {
        delegate.configureLogLevel(name, configuredLevel);
    }
}
