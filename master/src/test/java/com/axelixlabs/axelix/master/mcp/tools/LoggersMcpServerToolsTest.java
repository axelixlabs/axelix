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
package com.axelixlabs.axelix.master.mcp.tools;

import java.util.List;
import java.util.Map;

import okhttp3.mockwebserver.RecordedRequest;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import com.axelixlabs.axelix.common.contract.logger.LogLevelChangeRequest;
import com.axelixlabs.axelix.common.contract.logger.LoggersFeed;
import com.axelixlabs.axelix.common.contract.logger.LoggersFeedGroup;
import com.axelixlabs.axelix.common.contract.logger.LoggersFeedLogger;
import com.axelixlabs.axelix.common.contract.logger.LoggersGroupProfile;
import com.axelixlabs.axelix.common.contract.logger.SingleLoggerProfile;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LoggersMcpServerTools}.
 *
 * @author Nikita Kirillov
 */
class LoggersMcpServerToolsTest extends AbstractInstanceMcpServerToolsTest {

    private final JsonMapper objectMapper = new JsonMapper();

    @Test
    void shouldReturnAvailableLogLevels() {
        // given.
        stubAllLoggers(new LoggersFeed()
                .levels(List.of("OFF", "ERROR", "WARN", "INFO", "DEBUG", "TRACE"))
                .loggers(List.of())
                .groups(List.of()));

        // when.
        String result = callTool("getAvailableLoggingLevels", Map.of("instanceId", INSTANCE_ID));

        // then.
        assertThat(result).isEqualTo("[OFF, ERROR, WARN, INFO, DEBUG, TRACE]");
    }

    @Test
    void shouldReturnAllGroupsAsJson() {
        // given.
        LoggersFeedGroup web = feedGroup("web", "INFO", List.of("com.example.app.web"));
        LoggersFeedGroup sql = feedGroup("sql", null, List.of("com.example.app.sql"));
        stubAllLoggers(new LoggersFeed().levels(List.of()).loggers(List.of()).groups(List.of(web, sql)));

        // when.
        String result = callTool("getLoggerGroupsFeed", Map.of("instanceId", INSTANCE_ID));

        // then.
        // language=json
        assertThatJson(result).isEqualTo("""
                [
                  { "name": "web", "configuredLevel": "INFO", "members": ["com.example.app.web"] },
                  { "name": "sql", "configuredLevel": null, "members": ["com.example.app.sql"] }
                ]
                """);
    }

    @Test
    void shouldReturnAllLoggersAsJson() {
        // given.
        LoggersFeedLogger root = feedLogger("ROOT", "INFO", "INFO");
        LoggersFeedLogger appLogger = feedLogger("com.example.app", null, "INFO");
        stubAllLoggers(new LoggersFeed()
                .levels(List.of())
                .loggers(List.of(root, appLogger))
                .groups(List.of()));

        // when.
        String result = callTool("getLoggersFeed", Map.of("instanceId", INSTANCE_ID));

        // then.
        // language=json
        assertThatJson(result).isEqualTo("""
                [
                  {
                    "name": "ROOT",
                    "configuredLevel": "INFO",
                    "effectiveLevel": "INFO",
                    "fallbackLevel": null,
                    "temporaryLevelInitiatedAt": null,
                    "temporaryLevelRollsBackAt": null
                  },
                  {
                    "name": "com.example.app",
                    "configuredLevel": null,
                    "effectiveLevel": "INFO",
                    "fallbackLevel": null,
                    "temporaryLevelInitiatedAt": null,
                    "temporaryLevelRollsBackAt": null
                  }
                ]
                """);
    }

    @Nested
    class FindLoggersByName {

        @Test
        void shouldReturnLoggerFoundViaTheDirectEndpoint() throws InterruptedException {
            // given.
            SingleLoggerProfile appLogger = logger("com.example.app", "DEBUG", "DEBUG");
            instance.respondWith("/axelix-loggers/logger/com.example.app", objectMapper.writeValueAsString(appLogger));

            // when.
            String result =
                    callTool("findLoggersByName", Map.of("instanceId", INSTANCE_ID, "loggerName", "com.example.app"));

            // then.
            // language=json
            assertThatJson(result).isEqualTo("""
                    {
                      "com.example.app": {
                        "name": "com.example.app",
                        "configuredLevel": "DEBUG",
                        "effectiveLevel": "DEBUG",
                        "fallbackLevel": null,
                        "temporaryLevelInitiatedAt": null,
                        "temporaryLevelRollsBackAt": null
                      }
                    }
                    """);

            // and.
            assertThat(instance.takeRequest().getPath())
                    .isEqualTo(ACTUATOR_PATH + "/axelix-loggers/logger/com.example.app");
        }

        @Test
        void shouldFallBackToFilteringAllLoggersWhenDirectEndpointRespondsWithBadRequest() {
            // given. the exact-name lookup fails - the provided name is only a partial match.
            instance.respondWithBadRequest("/axelix-loggers/logger/com.example.app");

            // and.
            LoggersFeedLogger appService = feedLogger("com.example.app.service", null, "INFO");
            LoggersFeedLogger appRepository = feedLogger("com.example.app.repository", null, "INFO");
            LoggersFeedLogger root = feedLogger("ROOT", "INFO", "INFO");
            stubAllLoggers(new LoggersFeed()
                    .levels(List.of())
                    .loggers(List.of(appService, appRepository, root))
                    .groups(List.of()));

            // when.
            String result =
                    callTool("findLoggersByName", Map.of("instanceId", INSTANCE_ID, "loggerName", "com.example.app"));

            // then. only the loggers whose name contains the requested one are returned, keyed by the logger name.
            // language=json
            assertThatJson(result).isEqualTo("""
                    {
                      "com.example.app.service": {
                        "name": "com.example.app.service",
                        "configuredLevel": null,
                        "effectiveLevel": "INFO",
                        "fallbackLevel": null,
                        "temporaryLevelInitiatedAt": null,
                        "temporaryLevelRollsBackAt": null
                      },
                      "com.example.app.repository": {
                        "name": "com.example.app.repository",
                        "configuredLevel": null,
                        "effectiveLevel": "INFO",
                        "fallbackLevel": null,
                        "temporaryLevelInitiatedAt": null,
                        "temporaryLevelRollsBackAt": null
                      }
                    }
                    """);
        }

        @Test
        void shouldReturnEmptyMapWhenNoLoggerMatchesInTheFallback() {
            // given.
            instance.respondWithBadRequest("/axelix-loggers/logger/does-not-exist");

            // and.
            stubAllLoggers(new LoggersFeed()
                    .levels(List.of())
                    .loggers(List.of(feedLogger("ROOT", "INFO", "INFO")))
                    .groups(List.of()));

            // when.
            String result =
                    callTool("findLoggersByName", Map.of("instanceId", INSTANCE_ID, "loggerName", "does-not-exist"));

            // then.
            assertThat(result).isEqualTo("{}");
        }
    }

    @Nested
    class FindGroupsByName {

        @Test
        void shouldReturnGroupFoundViaTheDirectEndpoint() throws InterruptedException {
            // given.
            LoggersGroupProfile web = group("web", "WARN", List.of("com.example.app.web"));
            instance.respondWith("/axelix-loggers/group/web", objectMapper.writeValueAsString(web));

            // when.
            String result = callTool("findGroupsByName", Map.of("instanceId", INSTANCE_ID, "groupName", "web"));

            // then.
            // language=json
            assertThatJson(result).isEqualTo("""
                    {
                      "web": { "name": "web", "configuredLevel": "WARN", "members": ["com.example.app.web"] }
                    }
                    """);

            // and.
            assertThat(instance.takeRequest().getPath()).isEqualTo(ACTUATOR_PATH + "/axelix-loggers/group/web");
        }

        @Test
        void shouldFallBackToFilteringAllGroupsWhenDirectEndpointRespondsWithBadRequest() {
            // given.
            instance.respondWithBadRequest("/axelix-loggers/group/w");

            // and.
            LoggersFeedGroup web = feedGroup("web", "INFO", List.of("com.example.app.web"));
            LoggersFeedGroup sql = feedGroup("sql", null, List.of("com.example.app.sql"));
            stubAllLoggers(
                    new LoggersFeed().levels(List.of()).loggers(List.of()).groups(List.of(web, sql)));

            // when.
            String result = callTool("findGroupsByName", Map.of("instanceId", INSTANCE_ID, "groupName", "w"));

            // then. only the groups whose name contains the requested one are returned, keyed by the group name.
            // language=json
            assertThatJson(result).isEqualTo("""
                    {
                      "web": { "name": "web", "configuredLevel": "INFO", "members": ["com.example.app.web"] }
                    }
                    """);
        }

        @Test
        void shouldReturnEmptyMapWhenNoGroupMatchesInTheFallback() {
            // given.
            instance.respondWithBadRequest("/axelix-loggers/group/does-not-exist");

            // and.
            stubAllLoggers(new LoggersFeed()
                    .levels(List.of())
                    .loggers(List.of())
                    .groups(List.of(feedGroup("web", "INFO", List.of()))));

            // when.
            String result =
                    callTool("findGroupsByName", Map.of("instanceId", INSTANCE_ID, "groupName", "does-not-exist"));

            // then.
            assertThat(result).isEqualTo("{}");
        }
    }

    @Nested
    class ChangeLoggingLevelByLoggerName {

        @Test
        void shouldFanTheChangeOutToEveryListedInstance() throws InterruptedException {
            // when.
            String result = callTool(
                    "changeLoggingLevelByLoggerName",
                    Map.of(
                            "instanceIds",
                            List.of(INSTANCE_ID, ANOTHER_INSTANCE_ID),
                            "loggerName",
                            "com.example.app",
                            "loggerLevel",
                            "DEBUG",
                            "ttlSeconds",
                            120));

            // then.
            assertThat(result).isEqualTo(DONE_RESPONSE);

            // and.
            assertLevelChange(
                    instance.takeRequest(), "/axelix-loggers/logger/com.example.app/change-level", "DEBUG", 120L);
            assertLevelChange(
                    anotherInstance.takeRequest(),
                    "/axelix-loggers/logger/com.example.app/change-level",
                    "DEBUG",
                    120L);
        }

        @Test
        void shouldSendAPermanentChangeWhenTtlIsOmitted() throws InterruptedException {
            // when.
            String result = callTool(
                    "changeLoggingLevelByLoggerName",
                    Map.of("instanceIds", List.of(INSTANCE_ID), "loggerName", "ROOT", "loggerLevel", "WARN"));

            // then.
            assertThat(result).isEqualTo(DONE_RESPONSE);

            // and.
            assertLevelChange(instance.takeRequest(), "/axelix-loggers/logger/ROOT/change-level", "WARN", null);
        }

        @Test
        void shouldReportTheInstancesThatFailedToChangeTheLevel() throws InterruptedException {
            // given.
            anotherInstance.respondWithBadRequest("/axelix-loggers/logger/com.example.app/change-level");

            // when.
            String result = callToolExpectingError(
                    "changeLoggingLevelByLoggerName",
                    Map.of(
                            "instanceIds",
                            List.of(INSTANCE_ID, ANOTHER_INSTANCE_ID),
                            "loggerName",
                            "com.example.app",
                            "loggerLevel",
                            "DEBUG"));

            // then. the error message format is produced by the Spring AI MCP server toolkit, not by Axelix.
            assertThat(result)
                    .isEqualTo("Error invoking method: changeLoggingLevelByLoggerName" + System.lineSeparator()
                            + "The operation has failed for the following instances: instance-2");

            // and. the instance that succeeded still has the change applied.
            assertLevelChange(
                    instance.takeRequest(), "/axelix-loggers/logger/com.example.app/change-level", "DEBUG", null);
        }

        @Test
        void shouldRejectChangingTheLevelOnInstancesOfDifferentApplications() {
            // when.
            String result = callToolExpectingError(
                    "changeLoggingLevelByLoggerName",
                    Map.of(
                            "instanceIds",
                            List.of(INSTANCE_ID, ANOTHER_APPLICATION_INSTANCE_ID),
                            "loggerName",
                            "com.example.app",
                            "loggerLevel",
                            "DEBUG"));

            // then. the error message format is produced by the Spring AI MCP server toolkit, not by Axelix.
            assertThat(result)
                    .isEqualTo("Error invoking method: changeLoggingLevelByLoggerName" + System.lineSeparator()
                            + "All instances must belong to the same application");

            // and.
            assertThat(instance.requestCount()).isZero();
        }
    }

    @Nested
    class ChangeLoggingLevelByGroupName {

        @Test
        void shouldSendTheGroupNameAsThePathVariableAndAlwaysAPermanentChange() throws InterruptedException {
            // when.
            String result = callTool(
                    "changeLoggingLevelByGroupName",
                    Map.of("instanceId", INSTANCE_ID, "groupName", "web", "loggerLevel", "WARN"));

            // then.
            assertThat(result).isEqualTo(DONE_RESPONSE);

            // and.
            RecordedRequest request = instance.takeRequest();
            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getPath()).isEqualTo(ACTUATOR_PATH + "/axelix-loggers/group/web/change-level");

            LogLevelChangeRequest body =
                    objectMapper.readValue(request.getBody().readUtf8(), LogLevelChangeRequest.class);
            assertThat(body.getConfiguredLevel()).isEqualTo("WARN");
            assertThat(body.getTtlSeconds()).isNull();
        }
    }

    @Nested
    class ResetLoggingLevelByLoggerName {

        @Test
        void shouldFanTheResetOutToEveryListedInstance() throws InterruptedException {
            // when.
            String result = callTool(
                    "resetLoggingLevelByLoggerName",
                    Map.of("instanceIds", List.of(INSTANCE_ID, ANOTHER_INSTANCE_ID), "loggerName", "com.example.app"));

            // then.
            assertThat(result).isEqualTo(DONE_RESPONSE);

            // and.
            assertReset(instance.takeRequest());
            assertReset(anotherInstance.takeRequest());
        }

        @Test
        void shouldReportTheInstancesThatFailedToResetTheLevel() throws InterruptedException {
            // given.
            anotherInstance.respondWithBadRequest("/axelix-loggers/logger/com.example.app/reset");

            // when.
            String result = callToolExpectingError(
                    "resetLoggingLevelByLoggerName",
                    Map.of("instanceIds", List.of(INSTANCE_ID, ANOTHER_INSTANCE_ID), "loggerName", "com.example.app"));

            // then. the error message format is produced by the Spring AI MCP server toolkit, not by Axelix.
            assertThat(result)
                    .isEqualTo("Error invoking method: resetLoggingLevelByLoggerName" + System.lineSeparator()
                            + "The operation has failed for the following instances: instance-2");

            // and. the instance that succeeded still has the level reset.
            assertReset(instance.takeRequest());
        }

        @Test
        void shouldRejectResettingTheLevelOnInstancesOfDifferentApplications() {
            // when.
            String result = callToolExpectingError(
                    "resetLoggingLevelByLoggerName",
                    Map.of(
                            "instanceIds",
                            List.of(INSTANCE_ID, ANOTHER_APPLICATION_INSTANCE_ID),
                            "loggerName",
                            "com.example.app"));

            // then. the error message format is produced by the Spring AI MCP server toolkit, not by Axelix.
            assertThat(result)
                    .isEqualTo("Error invoking method: resetLoggingLevelByLoggerName" + System.lineSeparator()
                            + "All instances must belong to the same application");

            // and.
            assertThat(instance.requestCount()).isZero();
        }

        private static void assertReset(RecordedRequest request) {
            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getPath()).isEqualTo(ACTUATOR_PATH + "/axelix-loggers/logger/com.example.app/reset");
            assertThat(request.getBodySize()).isZero();
        }
    }

    private void assertLevelChange(
            RecordedRequest request, String path, String expectedLevel, @Nullable Long expectedTtlSeconds) {
        assertThat(request.getMethod()).isEqualTo("POST");
        assertThat(request.getPath()).isEqualTo(ACTUATOR_PATH + path);

        LogLevelChangeRequest body = objectMapper.readValue(request.getBody().readUtf8(), LogLevelChangeRequest.class);
        assertThat(body.getConfiguredLevel()).isEqualTo(expectedLevel);
        assertThat(body.getTtlSeconds()).isEqualTo(expectedTtlSeconds);
    }

    private void stubAllLoggers(LoggersFeed feed) {
        instance.respondWith("/axelix-loggers", objectMapper.writeValueAsString(feed));
    }

    private static SingleLoggerProfile logger(String name, String configuredLevel, String effectiveLevel) {
        return new SingleLoggerProfile()
                .name(name)
                .configuredLevel(configuredLevel)
                .effectiveLevel(effectiveLevel);
    }

    private static LoggersGroupProfile group(String name, String configuredLevel, List<String> members) {
        return new LoggersGroupProfile()
                .name(name)
                .configuredLevel(configuredLevel)
                .members(members);
    }

    private static LoggersFeedLogger feedLogger(String name, String configuredLevel, String effectiveLevel) {
        return new LoggersFeedLogger()
                .name(name)
                .configuredLevel(configuredLevel)
                .effectiveLevel(effectiveLevel);
    }

    private static LoggersFeedGroup feedGroup(String name, String configuredLevel, List<String> members) {
        return new LoggersFeedGroup()
                .name(name)
                .configuredLevel(configuredLevel)
                .members(members);
    }
}
