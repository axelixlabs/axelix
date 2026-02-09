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
package com.axelixlabs.axelix.master.api.external.endpoint;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.axelixlabs.axelix.common.api.loggers.LoggerGroup;
import com.axelixlabs.axelix.common.api.loggers.LoggerLevels;
import com.axelixlabs.axelix.common.api.loggers.ServiceLoggers;
import com.axelixlabs.axelix.common.domain.http.DefaultHttpPayload;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.request.LogLevelChangeRequest;
import com.axelixlabs.axelix.master.api.external.response.loggers.GroupProfileResponse;
import com.axelixlabs.axelix.master.api.external.response.loggers.LoggerProfileResponse;
import com.axelixlabs.axelix.master.api.external.response.loggers.LoggersResponse;
import com.axelixlabs.axelix.master.api.external.swagger.DefaultApiResponse;
import com.axelixlabs.axelix.master.api.external.swagger.InstanceIdParameter;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.convert.response.Converter;
import com.axelixlabs.axelix.master.service.serde.JacksonMessageSerializationStrategy;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for managing loggers.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@Tag(
        name = "Loggers API",
        description =
                "The loggers endpoint provides access to the application’s loggers and the configuration of their levels.")
@ExternalApiRestController
@RequestMapping(path = ApiPaths.LoggersApi.MAIN)
public class LoggersApi {

    private final EndpointInvoker endpointInvoker;
    private final Converter<ServiceLoggers, LoggersResponse> loggersResponseConverter;
    private final Converter<LoggerGroup, GroupProfileResponse> groupProfileConverter;
    private final Converter<LoggerLevels, LoggerProfileResponse> loggerProfileConverter;
    private final JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy;

    public LoggersApi(
            EndpointInvoker endpointInvoker,
            Converter<ServiceLoggers, LoggersResponse> loggersResponseConverter,
            Converter<LoggerGroup, GroupProfileResponse> groupProfileConverter,
            Converter<LoggerLevels, LoggerProfileResponse> loggerProfileConverter,
            JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy) {
        this.endpointInvoker = endpointInvoker;
        this.loggersResponseConverter = loggersResponseConverter;
        this.groupProfileConverter = groupProfileConverter;
        this.loggerProfileConverter = loggerProfileConverter;
        this.jacksonMessageSerializationStrategy = jacksonMessageSerializationStrategy;
    }

    @DefaultApiResponse(summary = "Returns the feed of the application’s loggers.")
    @ApiResponse(
            description = "OK",
            responseCode = "200",
            content =
                    @Content(mediaType = "application/json", schema = @Schema(implementation = LoggersResponse.class)))
    @InstanceIdParameter
    @GetMapping(path = ApiPaths.LoggersApi.INSTANCE_ID)
    public LoggersResponse getAllLoggers(@PathVariable("instanceId") String instanceId) {
        ServiceLoggers loggers = endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_ALL_LOGGERS, NoHttpPayload.INSTANCE);

        return Objects.requireNonNull(loggersResponseConverter.convert(loggers));
    }

    @DefaultApiResponse(summary = "Returns the profile of the of the requested logger group.")
    @ApiResponse(
            description = "OK",
            responseCode = "200",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GroupProfileResponse.class)))
    @InstanceIdParameter
    @Parameter(name = "groupName", description = "The name of the logger group to find", required = true)
    @GetMapping(path = ApiPaths.LoggersApi.GROUP_NAME)
    public GroupProfileResponse getGroupByName(
            @PathVariable("instanceId") String instanceId, @PathVariable("groupName") String groupName) {
        HttpPayload payload = new DefaultHttpPayload(Map.of("group.name", groupName));
        LoggerGroup group =
                endpointInvoker.invoke(InstanceId.of(instanceId), ActuatorEndpoints.GET_LOGGER_GROUP, payload);

        return Objects.requireNonNull(groupProfileConverter.convert(group));
    }

    @DefaultApiResponse(summary = "Returns the details of the requested logger.")
    @ApiResponse(
            description = "OK",
            responseCode = "200",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoggerProfileResponse.class)))
    @InstanceIdParameter
    @Parameter(name = "loggerName", description = "The name of the logger to find", required = true)
    @GetMapping(path = ApiPaths.LoggersApi.LOGGER_NAME)
    public LoggerProfileResponse getLoggerByName(
            @PathVariable("instanceId") String instanceId, @PathVariable("loggerName") String loggerName) {
        HttpPayload payload = new DefaultHttpPayload(Map.of("logger.name", loggerName));
        LoggerLevels logger =
                endpointInvoker.invoke(InstanceId.of(instanceId), ActuatorEndpoints.GET_ONE_LOGGER, payload);

        return Objects.requireNonNull(loggerProfileConverter.convert(logger));
    }

    @DefaultApiResponse(
            summary = "Change the logging level for a given logger by its name.",
            description =
                    "Suggested logging levels that the user can select to configure the logger: OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE")
    @ApiResponse(description = "OK", responseCode = "200")
    @InstanceIdParameter
    @Parameter(name = "loggerName", description = "The name of the logger to find", required = true)
    @PostMapping(path = ApiPaths.LoggersApi.LOGGER_NAME)
    public void setLoggingLevelByLoggerName(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("loggerName") String loggerName,
            @RequestBody LogLevelChangeRequest request) {

        HttpPayload payload = HttpPayload.json(
                Map.of("logger.name", loggerName), jacksonMessageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.SET_ONE_LOGGER, payload);
    }

    @DefaultApiResponse(
            summary = "The request specifies the desired logging level for a logger group by its name.",
            description =
                    "Suggested logging levels that the user can select to configure the logger: OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE")
    @ApiResponse(description = "OK", responseCode = "200")
    @InstanceIdParameter
    @Parameter(name = "groupName", description = "The name of the logger group to find", required = true)
    @PostMapping(path = ApiPaths.LoggersApi.GROUP_NAME)
    public void setLoggingLevelByGroupName(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("groupName") String groupName,
            @RequestBody LogLevelChangeRequest request) {

        HttpPayload payload = HttpPayload.json(
                Map.of("group.name", groupName), jacksonMessageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.SET_FOR_LOGGER_GROUP, payload);
    }

    @DefaultApiResponse(
            summary = "Clears the configured logging level of a logger, reverting it to the default setting")
    @ApiResponse(description = "OK", responseCode = "200")
    @InstanceIdParameter
    @Parameter(name = "loggerName", description = "The name of the logger to find", required = true)
    @PostMapping(path = ApiPaths.LoggersApi.CLEAR_FOR_LOGGER)
    public void clearLoggingLevelByLoggerName(
            @PathVariable("instanceId") String instanceId, @PathVariable("loggerName") String loggerName) {

        HttpPayload payload = HttpPayload.json(
                Map.of("logger.name", loggerName),
                jacksonMessageSerializationStrategy.serialize(Collections.emptyMap()));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.CLEAR_FOR_LOGGER, payload);
    }
}
