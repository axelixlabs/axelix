/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.master.api;

import java.util.Objects;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.api.ThreadDumpFeed;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.response.ThreadDumpFeedResponse;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.convert.response.Converter;
import com.nucleonforge.axile.master.service.transport.ThreadDumpEndpointProber;

/**
 * The API for thread dump.
 *
 * @since 18.11.2025
 * @author Nikita Kirillov
 */
@RestController
@RequestMapping(path = ApiPaths.ThreadDumpApi.MAIN)
public class ThreadDumpApi {

    private final ThreadDumpEndpointProber threadDumpEndpointProber;
    private final Converter<ThreadDumpFeed, ThreadDumpFeedResponse> converter;

    public ThreadDumpApi(
            ThreadDumpEndpointProber threadDumpEndpointProber,
            Converter<ThreadDumpFeed, ThreadDumpFeedResponse> converter) {
        this.threadDumpEndpointProber = threadDumpEndpointProber;
        this.converter = converter;
    }

    @GetMapping(ApiPaths.ThreadDumpApi.INSTANCE_ID)
    public ThreadDumpFeedResponse getThreadDump(@PathVariable("instanceId") String instanceId) {
        ThreadDumpFeed result = threadDumpEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);

        return Objects.requireNonNull(converter.convert(result));
    }
}
