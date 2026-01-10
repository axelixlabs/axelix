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
package com.nucleonforge.axelix.master.api;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axelix.common.api.gclog.GcLogEnableRequest;
import com.nucleonforge.axelix.common.domain.http.HttpPayload;
import com.nucleonforge.axelix.common.domain.http.NoHttpPayload;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.serde.JacksonMessageSerializationStrategy;
import com.nucleonforge.axelix.master.service.transport.gclog.DisableGcLoggingEndpointProber;
import com.nucleonforge.axelix.master.service.transport.gclog.EnableGcLoggingEndpointProber;
import com.nucleonforge.axelix.master.service.transport.gclog.GcLogFileEndpointProber;

/**
 * The API for GC logfile.
 *
 * @since 10.01.2026
 * @author Nikita Kirillov
 */
@RestController
@RequestMapping(path = ApiPaths.GcLogFileApi.MAIN)
public class GcLogFileApi {

    private final GcLogFileEndpointProber gcLogFileEndpointProber;
    private final EnableGcLoggingEndpointProber enableGcLoggingEndpointProber;
    private final DisableGcLoggingEndpointProber disableGcLoggingEndpointProber;
    private final JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy;

    public GcLogFileApi(
            GcLogFileEndpointProber gcLogFileEndpointProber,
            EnableGcLoggingEndpointProber enableGcLoggingEndpointProber,
            DisableGcLoggingEndpointProber disableGcLoggingEndpointProber,
            JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy) {
        this.gcLogFileEndpointProber = gcLogFileEndpointProber;
        this.enableGcLoggingEndpointProber = enableGcLoggingEndpointProber;
        this.disableGcLoggingEndpointProber = disableGcLoggingEndpointProber;
        this.jacksonMessageSerializationStrategy = jacksonMessageSerializationStrategy;
    }

    @GetMapping(path = ApiPaths.GcLogFileApi.INSTANCE_ID, produces = MediaType.TEXT_PLAIN_VALUE)
    public Resource getGcLogFile(@PathVariable("instanceId") String instanceId) {
        return gcLogFileEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
    }

    @GetMapping(path = ApiPaths.GcLogFileApi.ENABLE_GC_LOGGING)
    public void enableGcLogging(
            @PathVariable("instanceId") String instanceId, @RequestBody GcLogEnableRequest request) {

        HttpPayload httpPayload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        enableGcLoggingEndpointProber.invoke(InstanceId.of(instanceId), httpPayload);
    }

    @GetMapping(path = ApiPaths.GcLogFileApi.DISABLE_GC_LOGGING)
    public void disableGcLogging(@PathVariable("instanceId") String instanceId) {
        disableGcLoggingEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
    }
}
