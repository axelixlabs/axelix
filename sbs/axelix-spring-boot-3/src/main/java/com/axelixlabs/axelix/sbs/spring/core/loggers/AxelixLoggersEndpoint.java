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

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.actuate.logging.LoggersEndpoint.LoggerLevelsDescriptor;
import org.springframework.boot.actuate.logging.LoggersEndpoint.LoggersDescriptor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.axelixlabs.axelix.common.api.loggers.LogLevelChangeRequest;

/**
 * Custom Spring Boot Actuator endpoint exposing the application's loggers.
 *
 * @author Sergey Cherkasov
 */
@RestControllerEndpoint(id = "axelix-loggers")
public class AxelixLoggersEndpoint {

    private final LoggingSystem loggingSystem;
    private final LoggersEndpoint delegate;
    private final ConcurrentMap<String, LogLevel> cacheLoggers;

    public AxelixLoggersEndpoint(LoggingSystem loggingSystem, LoggersEndpoint delegate) {
        this.loggingSystem = loggingSystem;
        this.delegate = delegate;

        Map<String, LoggerLevelsDescriptor> loggers = delegate.loggers().getLoggers();
        this.cacheLoggers = new ConcurrentHashMap<>(loggers.size());

        loggers.forEach((name, levels) -> cacheLoggers.put(
                name, loggingSystem.getLoggerConfiguration(name).getEffectiveLevel()));
    }

    @GetMapping
    public LoggersDescriptor loggers() {
        return delegate.loggers();
    }

    @GetMapping("/{name}")
    public LoggerLevelsDescriptor loggerLevels(@PathVariable String name) {
        return delegate.loggerLevels(name);
    }

    @PostMapping("/{name}")
    public ResponseEntity<Void> configureLogLevel(
            @PathVariable String name, @RequestBody LogLevelChangeRequest request) {
        LogLevel logLevel = LogLevel.valueOf(request.getConfiguredLevel().toUpperCase(Locale.ROOT));
        delegate.configureLogLevel(name, logLevel);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset/{name}")
    public ResponseEntity<Void> resetLogLevel(@PathVariable String name) {
        LogLevel level = cacheLoggers.get(name);
        loggingSystem.setLogLevel(name, level);
        return ResponseEntity.noContent().build();
    }
}
