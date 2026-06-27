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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.boot.actuate.logging.LoggersEndpoint;
import org.springframework.boot.actuate.logging.LoggersEndpoint.LoggerLevelsDescriptor;
import org.springframework.boot.actuate.logging.LoggersEndpoint.LoggersDescriptor;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerConfiguration;
import org.springframework.boot.logging.LoggerGroups;
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
    private final LoggerGroups loggerGroups;
    private final LoggersEndpoint delegate;
    private final ConcurrentMap<String, LogLevel> cacheLoggers;

    public AxelixLoggersEndpoint(LoggingSystem loggingSystem, LoggerGroups loggerGroups) {
        this.loggingSystem = loggingSystem;
        this.loggerGroups = loggerGroups;
        this.delegate = new LoggersEndpoint(loggingSystem, loggerGroups);

        Map<String, LoggerLevelsDescriptor> loggers = delegate.loggers().getLoggers();
        this.cacheLoggers = loggers == null ? new ConcurrentHashMap<>() : new ConcurrentHashMap<>(loggers.size(), 1.1f);

        if (loggers != null) {
            loggers.forEach((name, levels) -> {
                LoggerConfiguration configuration = loggingSystem.getLoggerConfiguration(name);
                if (configuration != null) {
                    cacheLoggers.put(name, configuration.getEffectiveLevel());
                }
            });
        }
    }

    @GetMapping
    public LoggersDescriptor getAllLoggers() {
        return delegate.loggers();
    }

    @GetMapping("/logger/{name}")
    public ResponseEntity<LoggerLevelsDescriptor> getSingleLogger(@PathVariable String name) {
        if (!cacheLoggers.containsKey(name)) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(delegate.loggerLevels(name));
    }

    @GetMapping("/group/{name}")
    public ResponseEntity<LoggerLevelsDescriptor> getSingleGroup(@PathVariable String name) {
        if (loggerGroups.get(name) == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(delegate.loggerLevels(name));
    }

    @PostMapping("/logger/{name}/change-level")
    public ResponseEntity<Void> changeLogLevelByLoggerName(
            @PathVariable String name, @RequestBody LogLevelChangeRequest request) {

        if (!cacheLoggers.containsKey(name)) {
            return ResponseEntity.badRequest().build();
        }

        LogLevel logLevel = LogLevel.valueOf(request.getConfiguredLevel().toUpperCase());
        delegate.configureLogLevel(name, logLevel);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/group/{name}/change-level")
    public ResponseEntity<Void> changeLogLevelByGroupName(
            @PathVariable String name, @RequestBody LogLevelChangeRequest request) {
        if (loggerGroups.get(name) == null) {
            return ResponseEntity.badRequest().build();
        }

        LogLevel logLevel = LogLevel.valueOf(request.getConfiguredLevel().toUpperCase());
        delegate.configureLogLevel(name, logLevel);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logger/{name}/reset")
    public ResponseEntity<Void> resetLogLevelByLoggerName(@PathVariable String name) {
        if (!cacheLoggers.containsKey(name)) {
            return ResponseEntity.badRequest().build();
        }

        LogLevel level = cacheLoggers.get(name);

        LoggerConfiguration configuration = loggingSystem.getLoggerConfiguration(name);
        if (configuration != null && !configuration.getEffectiveLevel().equals(level)) {
            loggingSystem.setLogLevel(name, level);
        }

        return ResponseEntity.noContent().build();
    }
}
