/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.spring.gclog;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.nucleonforge.axelix.common.api.gclog.GcLogEnableRequest;
import com.nucleonforge.axelix.common.api.gclog.GcLogStatusResponse;

/**
 * Custom Actuator endpoint for managing and inspecting JVM GC logging.
 *
 * @since 28.12.2025
 * @author Nikita Kirillov
 */
@RestControllerEndpoint(id = "axelix-gclog")
public class GcLogEndpoint {

    private final GcLogService gcLogService;

    public GcLogEndpoint(GcLogService gcLogService) {
        this.gcLogService = gcLogService;
    }

    @GetMapping("/status")
    public GcLogStatusResponse status() {
        return gcLogService.getStatus();
    }

    @GetMapping(value = "/gc-logfile", produces = MediaType.TEXT_PLAIN_VALUE)
    public Resource gcLogfile() {
        return new FileSystemResource(gcLogService.getGcLogFile());
    }

    @PostMapping("/trigger")
    public void triggerGc() {
        System.gc();
    }

    @PostMapping("/enable")
    public void enable(@RequestBody GcLogEnableRequest request) {
        gcLogService.enable(request.level());
    }

    @PostMapping("/disable")
    public void disable() {
        gcLogService.disable();
    }
}
