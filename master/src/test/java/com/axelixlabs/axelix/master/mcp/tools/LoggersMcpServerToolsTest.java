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

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.json.JsonMapper;

import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.HttpPayload;
import com.axelixlabs.axelix.master.contract.logger.LogLevelChangeRequest;
import com.axelixlabs.axelix.master.contract.logger.LoggersFeed;
import com.axelixlabs.axelix.master.contract.logger.LoggersFeedGroup;
import com.axelixlabs.axelix.master.contract.logger.LoggersFeedLogger;
import com.axelixlabs.axelix.master.contract.logger.LoggersGroupProfile;
import com.axelixlabs.axelix.master.contract.logger.SingleLoggerProfile;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.serde.JacksonMessageSerializationStrategy;
import com.axelixlabs.axelix.master.service.transport.BadRequestException;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LoggersMcpServerTools}.
 *
 * @author Nikita Kirillov
 */
class LoggersMcpServerToolsTest {

    private static final String INSTANCE_ID = "instance-1";

    private final EndpointInvoker endpointInvoker = mock(EndpointInvoker.class);

    private final JsonMapper objectMapper = new JsonMapper();

    private final LoggersMcpServerTools subject = new LoggersMcpServerTools(
            endpointInvoker, new JacksonMessageSerializationStrategy(objectMapper), objectMapper);

    @Test
    void shouldReturnAvailableLogLevels() {
        // given.
        stubAllLoggers(new LoggersFeed()
                .levels(List.of("OFF", "ERROR", "WARN", "INFO", "DEBUG", "TRACE"))
                .loggers(List.of())
                .groups(List.of()));

        // when.
        String result = subject.getAvailableLoggingLevels(INSTANCE_ID);

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
        String result = subject.getLoggerGroupsFeed(INSTANCE_ID);

        // then.
        assertThatJson(result).isArray().hasSize(2);
        assertThatJson(result).node("[0].name").isEqualTo("web");
        assertThatJson(result).node("[0].configuredLevel").isEqualTo("INFO");
        assertThatJson(result).node("[0].members").isArray().containsExactly("com.example.app.web");
        assertThatJson(result).node("[1].name").isEqualTo("sql");
        assertThatJson(result).node("[1].configuredLevel").isEqualTo(null);
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
        String result = subject.getLoggersFeed(INSTANCE_ID);

        // then.
        assertThatJson(result).isArray().hasSize(2);
        assertThatJson(result).node("[0].name").isEqualTo("ROOT");
        assertThatJson(result).node("[0].configuredLevel").isEqualTo("INFO");
        assertThatJson(result).node("[0].effectiveLevel").isEqualTo("INFO");
        assertThatJson(result).node("[1].name").isEqualTo("com.example.app");
        assertThatJson(result).node("[1].configuredLevel").isEqualTo(null);
    }

    @Nested
    class FindLoggersByName {

        @Test
        void shouldReturnLoggerFoundViaTheDirectEndpoint() {
            // given.
            SingleLoggerProfile appLogger = logger("com.example.app", "DEBUG", "DEBUG");
            when(endpointInvoker.invoke(eq(InstanceId.of(INSTANCE_ID)), eq(ActuatorEndpoints.GET_ONE_LOGGER), any()))
                    .thenReturn(objectMapper.writeValueAsBytes(appLogger));

            // when.
            Map<String, String> result = subject.findLoggersByName(INSTANCE_ID, "com.example.app");

            // then.
            assertThat(result).containsOnlyKeys("com.example.app");
            assertThat(result.get("com.example.app")).contains("DEBUG");

            ArgumentCaptor<HttpPayload> payloadCaptor = ArgumentCaptor.forClass(HttpPayload.class);
            verify(endpointInvoker)
                    .invoke(
                            eq(InstanceId.of(INSTANCE_ID)),
                            eq(ActuatorEndpoints.GET_ONE_LOGGER),
                            payloadCaptor.capture());
            assertThat(payloadCaptor.getValue().pathVariableValues())
                    .containsExactly(Map.entry("name", "com.example.app"));
        }

        @Test
        void shouldFallBackToFilteringAllLoggersWhenDirectEndpointRespondsWithBadRequest() {
            // given. the exact-name lookup fails - the provided name is only a partial match.
            when(endpointInvoker.invoke(eq(InstanceId.of(INSTANCE_ID)), eq(ActuatorEndpoints.GET_ONE_LOGGER), any()))
                    .thenThrow(new BadRequestException("no such logger"));
            LoggersFeedLogger appService = feedLogger("com.example.app.service", null, "INFO");
            LoggersFeedLogger appRepository = feedLogger("com.example.app.repository", null, "INFO");
            LoggersFeedLogger root = feedLogger("ROOT", "INFO", "INFO");
            stubAllLoggers(new LoggersFeed()
                    .levels(List.of())
                    .loggers(List.of(appService, appRepository, root))
                    .groups(List.of()));

            // when.
            Map<String, String> result = subject.findLoggersByName(INSTANCE_ID, "com.example.app");

            // then.
            assertThat(result).containsOnlyKeys("com.example.app.service", "com.example.app.repository");
        }

        @Test
        void shouldReturnEmptyMapWhenNoLoggerMatchesInTheFallback() {
            // given.
            when(endpointInvoker.invoke(eq(InstanceId.of(INSTANCE_ID)), eq(ActuatorEndpoints.GET_ONE_LOGGER), any()))
                    .thenThrow(new BadRequestException("no such logger"));
            stubAllLoggers(new LoggersFeed()
                    .levels(List.of())
                    .loggers(List.of(feedLogger("ROOT", "INFO", "INFO")))
                    .groups(List.of()));

            // when.
            Map<String, String> result = subject.findLoggersByName(INSTANCE_ID, "does-not-exist");

            // then.
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindGroupsByName {

        @Test
        void shouldReturnGroupFoundViaTheDirectEndpoint() {
            // given.
            LoggersGroupProfile web = group("web", "WARN", List.of("com.example.app.web"));
            when(endpointInvoker.invoke(eq(InstanceId.of(INSTANCE_ID)), eq(ActuatorEndpoints.GET_LOGGER_GROUP), any()))
                    .thenReturn(objectMapper.writeValueAsBytes(web));

            // when.
            Map<String, String> result = subject.findGroupsByName(INSTANCE_ID, "web");

            // then.
            assertThat(result).containsOnlyKeys("web");
            assertThat(result.get("web")).contains("WARN");

            ArgumentCaptor<HttpPayload> payloadCaptor = ArgumentCaptor.forClass(HttpPayload.class);
            verify(endpointInvoker)
                    .invoke(
                            eq(InstanceId.of(INSTANCE_ID)),
                            eq(ActuatorEndpoints.GET_LOGGER_GROUP),
                            payloadCaptor.capture());
            assertThat(payloadCaptor.getValue().pathVariableValues()).containsExactly(Map.entry("name", "web"));
        }

        @Test
        void shouldFallBackToFilteringAllGroupsWhenDirectEndpointRespondsWithBadRequest() {
            // given.
            when(endpointInvoker.invoke(eq(InstanceId.of(INSTANCE_ID)), eq(ActuatorEndpoints.GET_LOGGER_GROUP), any()))
                    .thenThrow(new BadRequestException("no such group"));
            LoggersFeedGroup web = feedGroup("web", "INFO", List.of("com.example.app.web"));
            LoggersFeedGroup sql = feedGroup("sql", null, List.of("com.example.app.sql"));
            stubAllLoggers(
                    new LoggersFeed().levels(List.of()).loggers(List.of()).groups(List.of(web, sql)));

            // when.
            Map<String, String> result = subject.findGroupsByName(INSTANCE_ID, "w");

            // then.
            assertThat(result).containsOnlyKeys("web");
        }

        @Test
        void shouldReturnEmptyMapWhenNoGroupMatchesInTheFallback() {
            // given.
            when(endpointInvoker.invoke(eq(InstanceId.of(INSTANCE_ID)), eq(ActuatorEndpoints.GET_LOGGER_GROUP), any()))
                    .thenThrow(new BadRequestException("no such group"));
            stubAllLoggers(new LoggersFeed()
                    .levels(List.of())
                    .loggers(List.of())
                    .groups(List.of(feedGroup("web", "INFO", List.of()))));

            // when.
            Map<String, String> result = subject.findGroupsByName(INSTANCE_ID, "does-not-exist");

            // then.
            assertThat(result).isEmpty();
        }
    }

    @Nested
    class ChangeLoggingLevelByLoggerName {

        @Test
        void shouldSendTheLoggerNameAsThePathVariableAndTheLevelWithTtlAsTheBody() {
            // when.
            subject.changeLoggingLevelByLoggerName(INSTANCE_ID, "com.example.app", "DEBUG", 120L);

            // then.
            ArgumentCaptor<HttpPayload> payloadCaptor = ArgumentCaptor.forClass(HttpPayload.class);
            verify(endpointInvoker)
                    .invokeNoValue(
                            eq(InstanceId.of(INSTANCE_ID)),
                            eq(ActuatorEndpoints.SET_ONE_LOGGER),
                            payloadCaptor.capture());

            HttpPayload payload = payloadCaptor.getValue();
            assertThat(payload.pathVariableValues()).containsExactly(Map.entry("name", "com.example.app"));

            LogLevelChangeRequest request = objectMapper.readValue(payload.requestBody(), LogLevelChangeRequest.class);
            assertThat(request.getConfiguredLevel()).isEqualTo("DEBUG");
            assertThat(request.getTtlSeconds()).isEqualTo(120L);
        }

        @Test
        void shouldSendAPermanentChangeWhenTtlIsOmitted() {
            // when.
            subject.changeLoggingLevelByLoggerName(INSTANCE_ID, "ROOT", "WARN", null);

            // then.
            ArgumentCaptor<HttpPayload> payloadCaptor = ArgumentCaptor.forClass(HttpPayload.class);
            verify(endpointInvoker)
                    .invokeNoValue(
                            eq(InstanceId.of(INSTANCE_ID)),
                            eq(ActuatorEndpoints.SET_ONE_LOGGER),
                            payloadCaptor.capture());

            LogLevelChangeRequest request =
                    objectMapper.readValue(payloadCaptor.getValue().requestBody(), LogLevelChangeRequest.class);
            assertThat(request.getConfiguredLevel()).isEqualTo("WARN");
            assertThat(request.getTtlSeconds()).isNull();
        }
    }

    @Nested
    class ChangeLoggingLevelByGroupName {

        @Test
        void shouldSendTheGroupNameAsThePathVariableAndAlwaysAPermanentChange() {
            // when.
            subject.changeLoggingLevelByGroupName(INSTANCE_ID, "web", "WARN");

            // then.
            ArgumentCaptor<HttpPayload> payloadCaptor = ArgumentCaptor.forClass(HttpPayload.class);
            verify(endpointInvoker)
                    .invokeNoValue(
                            eq(InstanceId.of(INSTANCE_ID)),
                            eq(ActuatorEndpoints.SET_FOR_LOGGER_GROUP),
                            payloadCaptor.capture());

            HttpPayload payload = payloadCaptor.getValue();
            assertThat(payload.pathVariableValues()).containsExactly(Map.entry("name", "web"));

            LogLevelChangeRequest request = objectMapper.readValue(payload.requestBody(), LogLevelChangeRequest.class);
            assertThat(request.getConfiguredLevel()).isEqualTo("WARN");
            assertThat(request.getTtlSeconds()).isNull();
        }
    }

    @Nested
    class ResetLoggingLevelByLoggerName {

        @Test
        void shouldSendTheLoggerNameAsThePathVariableAndNoBody() {
            // when.
            subject.resetLoggingLevelByLoggerName(INSTANCE_ID, "com.example.app");

            // then.
            ArgumentCaptor<HttpPayload> payloadCaptor = ArgumentCaptor.forClass(HttpPayload.class);
            verify(endpointInvoker)
                    .invokeNoValue(
                            eq(InstanceId.of(INSTANCE_ID)),
                            eq(ActuatorEndpoints.RESET_FOR_LOGGER),
                            payloadCaptor.capture());

            HttpPayload payload = payloadCaptor.getValue();
            assertThat(payload.pathVariableValues()).containsExactly(Map.entry("name", "com.example.app"));
            assertThat(payload.hasBody()).isFalse();
        }
    }

    private void stubAllLoggers(LoggersFeed feed) {
        when(endpointInvoker.invoke(eq(InstanceId.of(INSTANCE_ID)), eq(ActuatorEndpoints.GET_ALL_LOGGERS), any()))
                .thenReturn(objectMapper.writeValueAsBytes(feed));
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
