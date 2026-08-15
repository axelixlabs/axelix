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
package com.axelixlabs.axelix.master.autoconfiguration.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.logging.LoggingSystemProperty;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link AxelixStructuredLoggingEnvironmentPostProcessor}.
 *
 * @author Dmitry Mazurov
 */
@ExtendWith(OutputCaptureExtension.class)
class AxelixStructuredLoggingIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(AxelixStructuredLoggingIntegrationTest.class);

    @AfterEach
    void reset(CapturedOutput output) {
        LoggingSystem.get(getClass().getClassLoader()).cleanUp();
        for (LoggingSystemProperty property : LoggingSystemProperty.values()) {
            System.getProperties().remove(property.getEnvironmentVariableName());
        }
        assertThat(output).doesNotContain("-INFO in ch.qos.logback.classic.LoggerContext");
    }

    @Test
    void shouldLogInJsonFormatWhenEnabled(CapturedOutput output) {
        // given.
        try (ConfigurableApplicationContext _ = new SpringApplicationBuilder(EmptyApplication.class)
                .web(WebApplicationType.NONE)
                .properties("axelix.master.logging.json.enabled=true", "axelix.master.environment=integration-test")
                .run()) {

            // when.
            logger.info("structured logging integration test message");
        }

        // then.
        String logLine = findLogLine(output, "structured logging integration test message");
        assertThatJson(logLine).isObject();
        assertThatJson(logLine).node("@timestamp").isPresent();
        assertThatJson(logLine).node("message").isEqualTo("structured logging integration test message");
        assertThatJson(logLine).node("log.level").isEqualTo("INFO");
        assertThatJson(logLine).node("log.logger").isEqualTo(logger.getName());
        assertThatJson(logLine)
                .node("process.pid")
                .isEqualTo(ProcessHandle.current().pid());
        assertThatJson(logLine)
                .node("process.thread.name")
                .isEqualTo(Thread.currentThread().getName());
        assertThatJson(logLine).node("service.name").isEqualTo("axelix");
        assertThatJson(logLine).node("service.environment").isEqualTo("integration-test");
        assertThatJson(logLine).node("ecs.version").asString().isEqualTo("8.11");
    }

    @Test
    void shouldNotIncludeEnvironmentWhenNotConfigured(CapturedOutput output) {
        // given.
        try (ConfigurableApplicationContext _ = new SpringApplicationBuilder(EmptyApplication.class)
                .web(WebApplicationType.NONE)
                .properties("axelix.master.logging.json.enabled=true")
                .run()) {

            // when.
            logger.info("structured logging without environment test message");
        }

        // then.
        String logLine = findLogLine(output, "structured logging without environment test message");
        assertThatJson(logLine).node("service.environment").isAbsent();
    }

    @Test
    void shouldLogPlainTextWhenDisabled(CapturedOutput output) {
        // given.
        try (ConfigurableApplicationContext _ = new SpringApplicationBuilder(EmptyApplication.class)
                .web(WebApplicationType.NONE)
                .properties("axelix.master.logging.json.enabled=false")
                .run()) {

            // when.
            logger.info("plain logging integration test message");
        }

        // then.
        String logLine = findLogLine(output, "plain logging integration test message");
        assertThatThrownBy(() -> new JsonMapper().readValue(logLine, JsonNode.class))
                .isInstanceOf(JacksonException.class);
    }

    private static String findLogLine(CapturedOutput output, String message) {
        return output.getOut()
                .lines()
                .filter(line -> line.contains(message))
                .findFirst()
                .orElseThrow();
    }

    @Configuration
    static class EmptyApplication {}
}
