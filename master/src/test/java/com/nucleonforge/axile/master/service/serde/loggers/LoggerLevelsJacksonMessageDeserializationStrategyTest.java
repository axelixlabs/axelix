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
package com.nucleonforge.axile.master.service.serde.loggers;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.loggers.LoggerLevels;

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
        String responseLoggerExample =
                """
            {
              "configuredLevel" : "DEBUG",
              "effectiveLevel" : "DEBUG"
            }
        """;

        // language=json
        String responseLoggerOrg =
                """
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
