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

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jspecify.annotations.Nullable;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.axelixlabs.axelix.common.api.loggers.LogLevelChangeRequest;
import com.axelixlabs.axelix.common.api.loggers.LoggersFeed;
import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.DefaultHttpPayload;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.api.error.SimpleApiError;
import com.axelixlabs.axelix.master.api.error.handle.ApiErrorCodes;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.request.loggers.LogLevelLoggerBulkChangeRequest;
import com.axelixlabs.axelix.master.api.external.response.loggers.GroupProfileResponse;
import com.axelixlabs.axelix.master.api.external.response.loggers.LoggerProfileResponse;
import com.axelixlabs.axelix.master.api.external.swagger.DefaultApiResponse;
import com.axelixlabs.axelix.master.api.external.swagger.InstanceIdParameter;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.serde.JacksonMessageSerializationStrategy;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;
import com.axelixlabs.axelix.master.service.transport.PartiallyUpdatedException;

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
public class LoggersApi {

    private final EndpointInvoker endpointInvoker;
    private final JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy;

    public LoggersApi(
            EndpointInvoker endpointInvoker, JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy) {
        this.endpointInvoker = endpointInvoker;
        this.jacksonMessageSerializationStrategy = jacksonMessageSerializationStrategy;
    }

    @DefaultApiResponse(summary = "Returns the feed of the application’s loggers.")
    @ApiResponse(
            description = "OK",
            responseCode = "200",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoggersFeed.class)))
    @InstanceIdParameter
    @GetMapping(path = ApiPaths.LoggersApi.INSTANCE_ID)
    public ResponseEntity<byte[]> getAllLoggers(@PathVariable("instanceId") String instanceId) {
        byte[] body = endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_ALL_LOGGERS, NoHttpPayload.INSTANCE);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
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
    public ResponseEntity<byte[]> getGroupByName(
            @PathVariable("instanceId") String instanceId, @PathVariable("groupName") String groupName) {
        HttpPayload payload = new DefaultHttpPayload(Map.of("name", groupName));
        byte[] body = endpointInvoker.invoke(InstanceId.of(instanceId), ActuatorEndpoints.GET_LOGGER_GROUP, payload);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
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
    public ResponseEntity<byte[]> getLoggerByName(
            @PathVariable("instanceId") String instanceId, @PathVariable("loggerName") String loggerName) {
        HttpPayload payload = new DefaultHttpPayload(Map.of("name", loggerName));
        byte[] body = endpointInvoker.invoke(InstanceId.of(instanceId), ActuatorEndpoints.GET_ONE_LOGGER, payload);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @DefaultApiResponse(
            summary = "Change the logging level for a given logger by its name across instances.",
            description =
                    "Suggested logging levels that the user can select to configure the logger: OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE")
    @ApiResponse(description = "No Content", responseCode = "204")
    @PostMapping(path = ApiPaths.LoggersApi.LOGGER_BULK_CHANGE)
    public ResponseEntity<?> setLoggingLevelByLoggerName(@RequestBody LogLevelLoggerBulkChangeRequest request) {

        if (request == null || isInvalid(request.instanceIds(), request.loggerName(), request.configuredLevel())) {
            return ResponseEntity.badRequest().build();
        }

        HttpPayload payload = HttpPayload.json(
                Map.of("name", request.loggerName()),
                jacksonMessageSerializationStrategy.serialize(
                        new LogLevelChangeRequest(request.configuredLevel(), request.ttlSeconds())));

        try {
            endpointInvoker.invokeNoValueForInstances(request.instanceIds(), ActuatorEndpoints.SET_ONE_LOGGER, payload);
            return ResponseEntity.noContent().build();
        } catch (PartiallyUpdatedException e) {
            return ResponseEntity.badRequest()
                    .body(new SimpleApiError(
                            ApiErrorCodes.PARTIALLY_UPDATED.getErrorCode(), HttpStatus.BAD_REQUEST.value()));
        }
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

        HttpPayload payload =
                HttpPayload.json(Map.of("name", groupName), jacksonMessageSerializationStrategy.serialize(request));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.SET_FOR_LOGGER_GROUP, payload);
    }

    @DefaultApiResponse(summary = "Reset the configured logging level of a logger, reverting it to the default setting")
    @ApiResponse(description = "No content", responseCode = "204")
    @InstanceIdParameter
    @Parameter(name = "loggerName", description = "The name of the logger to find", required = true)
    @PostMapping(path = ApiPaths.LoggersApi.RESET_FOR_LOGGER)
    public ResponseEntity<Void> resetLoggingLevelByLoggerName(
            @PathVariable("instanceId") String instanceId, @PathVariable("loggerName") String loggerName) {

        endpointInvoker.invokeNoValue(
                InstanceId.of(instanceId),
                ActuatorEndpoints.RESET_FOR_LOGGER,
                new DefaultHttpPayload(Map.of("name", loggerName)));

        return ResponseEntity.noContent().build();
    }

    private boolean isInvalid(
            @Nullable List<String> instanceIds, @Nullable String name, @Nullable String configuredLevel) {
        return CollectionUtils.isEmpty(instanceIds)
                || !StringUtils.hasText(name)
                || !StringUtils.hasText(configuredLevel);
    }
}
