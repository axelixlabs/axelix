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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.links.Link;
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
import com.axelixlabs.axelix.master.api.error.SimpleApiError;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.request.LogLevelChangeRequest;
import com.axelixlabs.axelix.master.api.external.response.loggers.GroupProfileResponse;
import com.axelixlabs.axelix.master.api.external.response.loggers.LoggerProfileResponse;
import com.axelixlabs.axelix.master.api.external.response.loggers.LoggersResponse;
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

    @Operation(
            summary = "Returns details of the application’s loggers.",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Loggers",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/loggers.html")
                        },
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = LoggersResponse.class))),
                @ApiResponse(
                        description = "Bad Request",
                        responseCode = "400",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class))),
                @ApiResponse(
                        description = "Internal Server Error",
                        responseCode = "500",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class)))
            })
    @Parameter(name = "instanceId", description = "Application Instance ID", required = true)
    @GetMapping(path = ApiPaths.LoggersApi.INSTANCE_ID)
    public LoggersResponse getAllLoggers(@PathVariable("instanceId") String instanceId) {
        ServiceLoggers loggers = endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_ALL_LOGGERS, NoHttpPayload.INSTANCE);

        return Objects.requireNonNull(loggersResponseConverter.convert(loggers));
    }

    @Operation(
            summary = "Returns details of the requested logger group.",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Loggers",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/loggers.html")
                        },
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = GroupProfileResponse.class))),
                @ApiResponse(
                        description = "Bad Request",
                        responseCode = "400",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class))),
                @ApiResponse(
                        description = "Internal Server Error",
                        responseCode = "500",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class)))
            })
    @Parameters({
        @Parameter(name = "instanceId", description = "Application Instance ID", required = true),
        @Parameter(name = "groupName", description = "The name of the logger group to find", required = true)
    })
    @GetMapping(path = ApiPaths.LoggersApi.GROUP_NAME)
    public GroupProfileResponse getGroupByName(
            @PathVariable("instanceId") String instanceId, @PathVariable("groupName") String groupName) {
        HttpPayload payload = new DefaultHttpPayload(Map.of("group.name", groupName));
        LoggerGroup group =
                endpointInvoker.invoke(InstanceId.of(instanceId), ActuatorEndpoints.GET_LOGGER_GROUP, payload);

        return Objects.requireNonNull(groupProfileConverter.convert(group));
    }

    @Operation(
            summary = "Returns the details of the requested logger.",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Loggers",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/loggers.html")
                        },
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = LoggerProfileResponse.class))),
                @ApiResponse(
                        description = "Bad Request",
                        responseCode = "400",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class))),
                @ApiResponse(
                        description = "Internal Server Error",
                        responseCode = "500",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class)))
            })
    @Parameters({
        @Parameter(name = "instanceId", description = "Application Instance ID", required = true),
        @Parameter(name = "loggerName", description = "The name of the logger to find", required = true)
    })
    @GetMapping(path = ApiPaths.LoggersApi.LOGGER_NAME)
    public LoggerProfileResponse getLoggerByName(
            @PathVariable("instanceId") String instanceId, @PathVariable("loggerName") String loggerName) {
        HttpPayload payload = new DefaultHttpPayload(Map.of("logger.name", loggerName));
        LoggerLevels logger =
                endpointInvoker.invoke(InstanceId.of(instanceId), ActuatorEndpoints.GET_ONE_LOGGER, payload);

        return Objects.requireNonNull(loggerProfileConverter.convert(logger));
    }

    @Operation(
            summary = "The request specifies the desired logging level for a logger by its name.",
            description =
                    "Suggested logging levels that the user can select to configure the logger: OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Loggers",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/loggers.html")
                        }),
                @ApiResponse(
                        description = "Bad Request",
                        responseCode = "400",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class))),
                @ApiResponse(
                        description = "Internal Server Error",
                        responseCode = "500",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class)))
            })
    @Parameters({
        @Parameter(name = "instanceId", description = "Application Instance ID", required = true),
        @Parameter(name = "loggerName", description = "The name of the logger to find", required = true)
    })
    @PostMapping(path = ApiPaths.LoggersApi.LOGGER_NAME)
    public void setLoggingLevelByLoggerName(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("loggerName") String loggerName,
            @RequestBody LogLevelChangeRequest request) {

        HttpPayload payload = HttpPayload.json(
                Map.of("logger.name", loggerName), jacksonMessageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.SET_ONE_LOGGER, payload);
    }

    @Operation(
            summary = "The request specifies the desired logging level for a logger group by its name.",
            description =
                    "Suggested logging levels that the user can select to configure the logger: OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Loggers",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/loggers.html")
                        }),
                @ApiResponse(
                        description = "Bad Request",
                        responseCode = "400",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class))),
                @ApiResponse(
                        description = "Internal Server Error",
                        responseCode = "500",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class)))
            })
    @Parameters({
        @Parameter(name = "instanceId", description = "Application Instance ID", required = true),
        @Parameter(name = "groupName", description = "The name of the logger group to find", required = true)
    })
    @PostMapping(path = ApiPaths.LoggersApi.GROUP_NAME)
    public void setLoggingLevelByGroupName(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("groupName") String groupName,
            @RequestBody LogLevelChangeRequest request) {

        HttpPayload payload = HttpPayload.json(
                Map.of("group.name", groupName), jacksonMessageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.SET_FOR_LOGGER_GROUP, payload);
    }

    @Operation(
            summary = "Clears the configured logging level of a logger, reverting it to the global/default setting.",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Loggers",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/loggers.html")
                        }),
                @ApiResponse(
                        description = "Bad Request",
                        responseCode = "400",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class))),
                @ApiResponse(
                        description = "Internal Server Error",
                        responseCode = "500",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class)))
            })
    @Parameters({
        @Parameter(name = "instanceId", description = "Application Instance ID", required = true),
        @Parameter(name = "loggerName", description = "The name of the logger to find", required = true)
    })
    @PostMapping(path = ApiPaths.LoggersApi.CLEAR_FOR_LOGGER)
    public void clearLoggingLevelByLoggerName(
            @PathVariable("instanceId") String instanceId, @PathVariable("loggerName") String loggerName) {

        HttpPayload payload = HttpPayload.json(
                Map.of("logger.name", loggerName),
                jacksonMessageSerializationStrategy.serialize(Collections.emptyMap()));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.CLEAR_FOR_LOGGER, payload);
    }
}
