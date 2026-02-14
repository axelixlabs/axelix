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
package com.axelixlabs.axelix.sbs.spring.core.gclog;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.axelixlabs.axelix.common.api.gclog.GcLogEnableRequest;
import com.axelixlabs.axelix.common.api.gclog.GcLogStatusResponse;

/**
 * Custom Actuator endpoint for managing and inspecting JVM GC logging.
 *
 * @since 28.12.2025
 * @author Nikita Kirillov
 */
@RestControllerEndpoint(id = "axelix-gc")
public class AxelixGcEndpoint {

    private final GcLogService gcLogService;

    public AxelixGcEndpoint(GcLogService gcLogService) {
        this.gcLogService = gcLogService;
    }

    @GetMapping("/log/status")
    public GcLogStatusResponse status() {
        return gcLogService.getStatus();
    }

    @GetMapping(value = "log/file", produces = MediaType.TEXT_PLAIN_VALUE)
    public Resource gcLogfile() {
        return new FileSystemResource(gcLogService.getGcLogFile());
    }

    @PostMapping("/trigger")
    public void triggerGc() {
        System.gc();
    }

    @PostMapping("/log/enable")
    public void enable(@RequestBody GcLogEnableRequest request) {
        gcLogService.enable(request.getLevel());
    }

    @PostMapping("/log/disable")
    public void disable() {
        gcLogService.disable();
    }
}
