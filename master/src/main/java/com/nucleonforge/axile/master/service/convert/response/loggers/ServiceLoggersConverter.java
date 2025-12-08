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
package com.nucleonforge.axile.master.service.convert.response.loggers;

import java.util.List;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.api.loggers.ServiceLoggers;
import com.nucleonforge.axile.master.api.response.loggers.LoggersResponse;
import com.nucleonforge.axile.master.service.convert.response.Converter;

/**
 * The {@link Converter} from {@link ServiceLoggers} to {@link LoggersResponse}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class ServiceLoggersConverter implements Converter<ServiceLoggers, LoggersResponse> {

    @Override
    public @NonNull LoggersResponse convertInternal(@NonNull ServiceLoggers source) {

        List<LoggersResponse.Group> groups = convertGroup(source);
        List<LoggersResponse.Logger> loggers = convertLogger(source);

        return new LoggersResponse(source.levels(), groups, loggers);
    }

    private List<LoggersResponse.Logger> convertLogger(ServiceLoggers source) {
        return source.loggers().entrySet().stream()
                .map(logger -> new LoggersResponse.Logger(
                        logger.getKey(),
                        logger.getValue().configuredLevel(),
                        logger.getValue().effectiveLevel()))
                .toList();
    }

    private List<LoggersResponse.Group> convertGroup(ServiceLoggers source) {
        return source.groups().entrySet().stream()
                .map(group -> new LoggersResponse.Group(
                        group.getKey(),
                        group.getValue().configuredLevel(),
                        group.getValue().members()))
                .toList();
    }
}
