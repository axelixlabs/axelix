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
package com.axelixlabs.axelix.master.service.convert.loggers;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.loggers.LoggerGroup;
import com.axelixlabs.axelix.common.api.loggers.LoggerLevels;
import com.axelixlabs.axelix.common.api.loggers.ServiceLoggers;
import com.axelixlabs.axelix.master.api.external.response.loggers.LoggersResponse;
import com.axelixlabs.axelix.master.service.convert.response.loggers.ServiceLoggersConverter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ServiceLoggersConverter}
 *
 * @author Sergey Cherkasov
 */
public class ServiceLoggersConverterTest {

    private ServiceLoggersConverter subject;

    @BeforeEach
    void setUp() {
        subject = new ServiceLoggersConverter();
    }

    @Test
    void testConvertHappyPath() {
        // when.
        LoggersResponse response = subject.convertInternal(getLoggers());

        // then
        assertThat(response).isNotNull();

        // levels
        assertThat(response.levels())
                .containsExactlyInAnyOrder("OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE");

        // loggers
        assertThat(response.loggers()).hasSize(3);
        List<LoggersResponse.Logger> loggers = response.loggers();

        // loggers -> "ROOT"
        assertThat(loggers)
                .filteredOn(logger -> logger.name().equals("ROOT"))
                .first()
                .satisfies(logger -> assertThat(logger.configuredLevel()).isEqualTo("INFO"))
                .satisfies(logger -> assertThat(logger.effectiveLevel()).isEqualTo("INFO"));

        // loggers -> "com.example"
        assertThat(loggers)
                .filteredOn(logger -> logger.name().equals("com.example"))
                .first()
                .satisfies(logger -> assertThat(logger.configuredLevel()).isEqualTo("DEBUG"))
                .satisfies(logger -> assertThat(logger.effectiveLevel()).isEqualTo("DEBUG"));

        // loggers ->
        assertThat(loggers)
                .filteredOn(logger -> logger.name().equals("org"))
                .first()
                .satisfies(logger -> assertThat(logger.configuredLevel()).isNull())
                .satisfies(logger -> assertThat(logger.effectiveLevel()).isEqualTo("INFO"));

        // groups
        assertThat(response.groups()).hasSize(3);
        List<LoggersResponse.Group> groups = response.groups();

        // groups -> "test"
        assertThat(groups)
                .filteredOn(group -> group.name().equals("test"))
                .first()
                .satisfies(group -> assertThat(group.configuredLevel()).isEqualTo("INFO"))
                .satisfies(
                        group -> assertThat(group.members()).containsExactlyInAnyOrder("test.member1", "test.member2"));

        // groups -> "web"
        assertThat(groups)
                .filteredOn(group -> group.name().equals("web"))
                .first()
                .satisfies(group -> assertThat(group.configuredLevel()).isNull())
                .satisfies(group -> assertThat(group.members())
                        .containsExactlyInAnyOrder(
                                "org.springframework.core.codec",
                                "org.springframework.http",
                                "org.springframework.web",
                                "org.springframework.boot.actuate.endpoint.web",
                                "org.springframework.boot.web.servlet.ServletContextInitializerBeans"));

        // groups -> "sql"
        assertThat(groups)
                .filteredOn(group -> group.name().equals("sql"))
                .first()
                .satisfies(group -> assertThat(group.configuredLevel()).isNull())
                .satisfies(group -> assertThat(group.members())
                        .containsExactlyInAnyOrder(
                                "org.springframework.jdbc.core", "org.hibernate.SQL", "org.jooq.tools.LoggerListener"));
    }

    private static ServiceLoggers getLoggers() {
        // levels
        List<String> levels = List.of("OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE");

        // loggers
        Map<String, LoggerLevels> loggers = Map.of(
                "ROOT",
                new LoggerLevels("INFO", "INFO"),
                "com.example",
                new LoggerLevels("DEBUG", "DEBUG"),
                "org",
                new LoggerLevels(null, "INFO"));

        // groups
        Map<String, LoggerGroup> groups = Map.of(
                "test", new LoggerGroup("INFO", List.of("test.member1", "test.member2")),
                "web",
                        new LoggerGroup(
                                null,
                                List.of(
                                        "org.springframework.core.codec",
                                        "org.springframework.http",
                                        "org.springframework.web",
                                        "org.springframework.boot.actuate.endpoint.web",
                                        "org.springframework.boot.web.servlet.ServletContextInitializerBeans")),
                "sql",
                        new LoggerGroup(
                                null,
                                List.of(
                                        "org.springframework.jdbc.core",
                                        "org.hibernate.SQL",
                                        "org.jooq.tools.LoggerListener")));

        return new ServiceLoggers(levels, loggers, groups);
    }
}
