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

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.axelixlabs.axelix.common.api.loggers.LogLevelChangeRequest;
import com.axelixlabs.axelix.common.api.loggers.LoggersFeed;
import com.axelixlabs.axelix.common.api.loggers.LoggersGroupProfile;
import com.axelixlabs.axelix.common.api.loggers.SingleLoggerProfile;
import com.axelixlabs.axelix.sbs.spring.core.loggers.exceptions.LogLevelNotFoundException;
import com.axelixlabs.axelix.sbs.spring.core.loggers.exceptions.LoggerNotFoundException;

/**
 * Custom Spring Boot Actuator endpoint exposing the application's loggers.
 *
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
@RestControllerEndpoint(id = "axelix-loggers")
public class AxelixLoggersEndpoint {

    private final LoggersService loggersService;

    public AxelixLoggersEndpoint(LoggersService loggersService) {
        this.loggersService = loggersService;
    }

    @GetMapping
    public ResponseEntity<LoggersFeed> getAllLoggers() {
        return ResponseEntity.ok(loggersService.getAllLoggers());
    }

    @GetMapping("/logger/{name}")
    public ResponseEntity<SingleLoggerProfile> getSingleLogger(@PathVariable String name) {
        try {
            SingleLoggerProfile singleLoggerProfile = loggersService.getSingleLogger(name);
            return ResponseEntity.ok(singleLoggerProfile);
        } catch (LoggerNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/group/{name}")
    public ResponseEntity<LoggersGroupProfile> getLoggerGroup(@PathVariable String name) {
        try {
            LoggersGroupProfile loggerGroup = loggersService.getLoggerGroup(name);
            return ResponseEntity.ok(loggerGroup);
        } catch (LoggerNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/logger/{name}/change-level")
    public ResponseEntity<Void> changeLogLevelByLoggerName(
            @PathVariable String name, @RequestBody LogLevelChangeRequest changeRequest) {
        try {
            loggersService.changeLogLevelByLoggerName(name, changeRequest);
            return ResponseEntity.noContent().build();
        } catch (LoggerNotFoundException | LogLevelNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/group/{name}/change-level")
    public ResponseEntity<Void> changeLogLevelByGroupName(
            @PathVariable String name, @RequestBody LogLevelChangeRequest changeRequest) {
        try {
            loggersService.changeLogLevelByGroupName(name, changeRequest);
            return ResponseEntity.noContent().build();
        } catch (LoggerNotFoundException | LogLevelNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/logger/{name}/reset")
    public ResponseEntity<Void> resetLogLevelByLoggerName(@PathVariable String name) {
        try {
            loggersService.resetLogLevelByLoggerName(name);
            return ResponseEntity.noContent().build();
        } catch (LoggerNotFoundException | LogLevelNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
