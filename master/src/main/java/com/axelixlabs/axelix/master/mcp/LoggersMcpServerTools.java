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
package com.axelixlabs.axelix.master.mcp;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.api.loggers.LogLevelChangeRequest;
import com.axelixlabs.axelix.common.api.loggers.LoggerGroup;
import com.axelixlabs.axelix.common.api.loggers.LoggerLevels;
import com.axelixlabs.axelix.common.api.loggers.ServiceLoggers;
import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.DefaultHttpPayload;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.serde.JacksonMessageSerializationStrategy;
import com.axelixlabs.axelix.master.service.transport.BadRequestException;
import com.axelixlabs.axelix.master.service.transport.EndpointInvocationException;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

@Service
public class LoggersMcpServerTools {

    private final EndpointInvoker endpointInvoker;

    private final JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy;

    public LoggersMcpServerTools(
            EndpointInvoker endpointInvoker, JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy) {
        this.endpointInvoker = endpointInvoker;
        this.jacksonMessageSerializationStrategy = jacksonMessageSerializationStrategy;
    }

    @McpTool(
            title = "Available Logging Levels",
            description = """
            Get the list of available logging levels for a specific instance.

            Present the results as a numbered list.

            Use this ONLY when when you want to understand what logging levels are available.
            Do NOT use this to browse loggers — use "Find Loggers" instead.

            Because this Tool accepts "Instance ID" you probably will need to call 'getWallboard'
            tool to first retrieve the instances feed.
            """,
            annotations =
                    @McpTool.McpAnnotations(
                            title = "Provides the list of available logging levels inside this Spring Boot app",
                            readOnlyHint = true,
                            destructiveHint = false,
                            idempotentHint = true,
                            openWorldHint = false))
    public String getAvailableLoggingLevels(@McpToolParam(description = "The instance ID") String instanceId) {
        return getAllLoggers(instanceId).levels().toString();
    }

    @McpTool(
            title = "Logger Groups Feed",
            description = """
            Get the full list of configured logger groups with their members and logging levels for a specific instance.

            For each group, clearly list its name, configured level, and all members belonging to that group.

            Do NOT use this when the user mentions a specific group name — use "Find Loggers Group" instead.

            Because this Tool accepts "Instance ID" you probably will need to call 'getWallboard'
            tool to first retrieve the instances feed.
            """,
            annotations =
                    @McpTool.McpAnnotations(
                            title = "Provides a list of logging groups within this Spring Boot app",
                            readOnlyHint = true,
                            destructiveHint = false,
                            idempotentHint = true,
                            openWorldHint = false))
    public String getLoggerGroupsFeed(@McpToolParam(description = "The instance ID") String instanceId) {
        return getAllLoggers(instanceId).groups().toString();
    }

    @McpTool(
            title = "Find Logger",
            description = """
            Get one or more individual loggers by name.

            Use ONLY for individual loggers, NOT for logger groups.

            Use this tool as the STARTING POINT when a specific logger is mentioned by name to:
            1. Return information about a specific logger if the user provided an exact name.
            2. Return a map of matching loggers if the user provided an approximate name — then ask the user
               to choose a specific one before proceeding further.

            If the user wants to change the logging level after finding the logger, use the "Set Logger Level" tool.
            If the returned map is empty, do NOT search in logger groups. Inform the user that no loggers matching the
            given name were found and suggest to try a different name.
            Do NOT summarize or truncate the results.

            Because this Tool accepts "Instance ID" you probably will need to call 'getWallboard'
            tool to first retrieve the instances feed.
            """,
            annotations =
                    @McpTool.McpAnnotations(
                            title = "Shows information about a logger by its name (or assumed name)",
                            readOnlyHint = true,
                            destructiveHint = false,
                            idempotentHint = true,
                            openWorldHint = false))
    public Map<String, String> findLoggersByName(
            @McpToolParam(description = "The instance ID") String instanceId,
            @McpToolParam(description = """
                The Logger Name. The exact or approximate name of the logger.
                Partial names are supported.""") String loggerName) {

        try {
            HttpPayload payload = new DefaultHttpPayload(Map.of("logger.name", loggerName));

            LoggerLevels logger =
                    endpointInvoker.invoke(InstanceId.of(instanceId), ActuatorEndpoints.GET_ONE_LOGGER, payload);

            return Map.of(loggerName, logger.toString());
        } catch (EndpointInvocationException | BadRequestException e) {
            return getAllLoggers(instanceId).loggers().entrySet().stream()
                    .filter(entry -> entry.getKey().contains(loggerName))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, entry -> entry.getValue().toString()));
        }
    }

    @McpTool(
            title = "Find Loggers Group",
            description = """
            Get one or more logger groups by name.

            Use ONLY for logger groups, NOT for individual loggers.

            Use this tool as the STARTING POINT when a specific logger group is mentioned by name to:
            1. Return information about a specific logger group if the user provided an exact name.
            2. Return a map of matching logger groups if the user provided an approximate name — then ask the user
               to choose a specific one before proceeding further.

            If the user wants to change the logging level after finding the group, use the "Set Group Loggers Level" tool.
            If the returned map is empty, do NOT search in loggers. Inform the user that no logger groups matching the
            given name were found and suggest to try a different name.
            For each group, clearly list its name, configured level, and all members belonging to that group.
            Do NOT summarize or truncate the results.

            Because this Tool accepts "Instance ID" you probably will need to call 'getWallboard'
            tool to first retrieve the instances feed.
            """,
            annotations =
                    @McpTool.McpAnnotations(
                            title = "Shows information about a logger group by its name (or assumed name)",
                            readOnlyHint = true,
                            destructiveHint = false,
                            idempotentHint = true,
                            openWorldHint = false))
    public Map<String, String> findGroupsByName(
            @McpToolParam(description = "The instance ID") String instanceId,
            @McpToolParam(description = """
                The Group Name. The exact or approximate name of the group loggers.
                Partial names are supported.""") String groupName) {
        try {
            HttpPayload payload = new DefaultHttpPayload(Map.of("group.name", groupName));

            LoggerGroup logger =
                    endpointInvoker.invoke(InstanceId.of(instanceId), ActuatorEndpoints.GET_LOGGER_GROUP, payload);

            return Map.of(groupName, logger.toString());
        } catch (EndpointInvocationException | BadRequestException e) {
            return getAllLoggers(instanceId).groups().entrySet().stream()
                    .filter(entry -> entry.getKey().contains(groupName))
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, entry -> entry.getValue().toString()));
        }
    }

    @McpTool(
            title = "Change Logger Level",
            description = """
            Changes the logging level of a specific individual logger.

            Use this ONLY when:
            - The user explicitly asks to change the logging level of a specific individual logger.

            Do NOT use this tool for logger groups — use "Set Group Loggers Level" instead.

            If the invocation succeeds, do NOT ask for confirmation.

            If the invocation fails:
            1. Call the "Find Loggers" tool with the provided logger name.
            2. If multiple loggers are returned, present them to the user as a numbered list
               and ask them to choose one by number or name.
            3. If a single logger is returned, ask the user to confirm that this is the logger they meant
               before retrying the invocation.
            4. If no loggers are found, inform the user that no such logger exists.

            If the logger level is not specified by the user, call the "Available Logging Levels" tool to get the list
            of available levels, present them to the user as a numbered list, and ask them to choose one before
            calling this tool.

            Because this Tool accepts "Instance ID" you probably will need to call 'getWallboard'
            tool to first retrieve the instances feed.
            """,
            annotations =
                    @McpTool.McpAnnotations(
                            title = "Changes the logging level of a specific logger by its name",
                            readOnlyHint = false,
                            destructiveHint = true,
                            idempotentHint = true,
                            openWorldHint = false))
    public void changeLoggingLevelByLoggerName(
            @McpToolParam(description = "The instance ID") String instanceId,
            @McpToolParam(description = """
                The Logger Name. The exact or approximate name of the logger.
                Partial names are supported.""") String loggerName,
            @McpToolParam(description = "The logging level.") String loggerLevel) {

        HttpPayload payload = HttpPayload.json(
                Map.of("logger.name", loggerName),
                jacksonMessageSerializationStrategy.serialize(new LogLevelChangeRequest(loggerLevel)));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.SET_ONE_LOGGER, payload);
    }

    @McpTool(
            title = "Change Group Loggers Level",
            description = """
            Changes the logging level of a specific logger group.

            Use this ONLY when:
            - The user explicitly asks to change the logging level of a specific logger group.

            Do NOT use this tool for individual loggers — use "Set Logger Level" instead.

            If the invocation succeeds, do NOT ask for confirmation.

            If the invocation fails:
            1. Call the "Find Loggers Group" tool with the provided group name.
            2. If multiple groups are returned, present them to the user as a numbered list
               and ask them to choose one by number or name.
            3. If a single group is returned, ask the user to confirm that this is the group they meant
               before retrying the invocation.
            4. If no groups are found, inform the user that no such logger group exists.

            If the logger level is not specified by the user, call the "Available Logging Levels" tool to get the list
            of available levels, present them to the user as a numbered list, and ask them to choose one before
            calling this tool.

            Because this Tool accepts "Instance ID" you probably will need to call 'getWallboard'
            tool to first retrieve the instances feed.
            """,
            annotations =
                    @McpTool.McpAnnotations(
                            title = "Changes the logging level of a specific logger group by its name",
                            readOnlyHint = false,
                            destructiveHint = true,
                            idempotentHint = true,
                            openWorldHint = false))
    public void changeLoggingLevelByGroupName(
            @McpToolParam(description = "The instance ID") String instanceId,
            @McpToolParam(description = """
                The Group Name. The exact or approximate name of the group loggers.
                Partial names are supported.""") String groupName,
            @McpToolParam(description = "The logging level.") String loggerLevel) {

        HttpPayload payload = HttpPayload.json(
                Map.of("group.name", groupName),
                jacksonMessageSerializationStrategy.serialize(new LogLevelChangeRequest(loggerLevel)));
        endpointInvoker.invokeNoValue(InstanceId.of(instanceId), ActuatorEndpoints.SET_FOR_LOGGER_GROUP, payload);
    }

    @McpTool(
            title = "Reset Loggers Level",
            description = """
            Resets the logging level of a specific individual logger to its default value.
            Resets to the default level defined in the application configuration.
            Use this ONLY when:
            - The user explicitly asks to reset or restore the logging level of a specific individual logger.

            Do NOT use this tool to change or modify a logging level — use "Set Logger Level" instead.
            Do NOT use this tool for logger groups — resetting group logging level is not supported.
            If the user asks to reset a logger group level, inform them that this operation is not available.

            If the invocation succeeds, do NOT ask for confirmation.

            If the invocation fails:
            1. Call the "Find Loggers" tool with the provided logger name.
            2. If multiple loggers are returned, present them to the user as a numbered list
               and ask them to choose one by number or name.
            3. If a single logger is returned, ask the user to confirm that this is the logger they meant
               before retrying the invocation.
            4. If no loggers are found, inform the user that no such logger exists.

            Because this Tool accepts "Instance ID" you probably will need to call 'getWallboard'
            tool to first retrieve the instances feed.
            """,
            annotations =
                    @McpTool.McpAnnotations(
                            title = "Resets the logging level for a specific logger by its name",
                            readOnlyHint = false,
                            destructiveHint = true,
                            idempotentHint = true,
                            openWorldHint = false))
    public void resetLoggingLevelByLoggerName(
            @McpToolParam(description = "The instance ID") String instanceId,
            @McpToolParam(description = """
                The Logger Name. The exact or approximate name of the logger.
                Partial names are supported.""") String loggerName) {

        endpointInvoker.invokeNoValue(
                InstanceId.of(instanceId),
                ActuatorEndpoints.RESET_FOR_LOGGER,
                new DefaultHttpPayload(Map.of("logger.name", loggerName)));
    }

    private ServiceLoggers getAllLoggers(String instanceId) {
        return endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_ALL_LOGGERS, NoHttpPayload.INSTANCE);
    }
}
