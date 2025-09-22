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
