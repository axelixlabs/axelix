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
package com.axelixlabs.axelix.master.service.serde.loggers;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.loggers.LoggerLevels;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LoggerLevelsJacksonMessageDeserializationStrategy}.
 *
 * @author Sergey Cherkasov
 */
public class LoggerLevelsJacksonMessageDeserializationStrategyTest {
    private final LoggerLevelsJacksonMessageDeserializationStrategy subject =
            new LoggerLevelsJacksonMessageDeserializationStrategy(new ObjectMapper());

    @Test
    void shouldDeserializeLoggerLoggers() {
        // language=json
        String responseLoggerExample = """
            {
              "configuredLevel" : "DEBUG",
              "effectiveLevel" : "DEBUG"
            }
        """;

        // language=json
        String responseLoggerOrg = """
            {
              "effectiveLevel" : "INFO"
            }
            """;

        // when.
        LoggerLevels loggerExample = subject.deserialize(responseLoggerExample.getBytes(StandardCharsets.UTF_8));
        LoggerLevels loggerOrg = subject.deserialize(responseLoggerOrg.getBytes(StandardCharsets.UTF_8));

        // loggerExample
        assertThat(loggerExample.configuredLevel()).isEqualTo("DEBUG");
        assertThat(loggerExample.effectiveLevel()).isEqualTo("DEBUG");

        // loggerOrg
        assertThat(loggerOrg.configuredLevel()).isNull();
        assertThat(loggerOrg.effectiveLevel()).isEqualTo("INFO");
    }
}
