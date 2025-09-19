package com.nucleonforge.axile.master.service.convert.loggers;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.loggers.LoggerLoggers;
import com.nucleonforge.axile.master.api.response.loggers.LoggerProfile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LoggerLoggersConverter}
 *
 * @author Sergey Cherkasov
 */
public class LoggerLoggersConverterTest {
    private final LoggerLoggersConverter subject = new LoggerLoggersConverter();

    @Test
    void testConvertHappyPath() {
        LoggerLoggers loggersInfo = new LoggerLoggers(null, "INFO");
        LoggerLoggers loggersDebug = new LoggerLoggers("DEBUG", "DEBUG");

        // when.
        LoggerProfile infoResponse = subject.convertInternal(loggersInfo);
        LoggerProfile debugResponse = subject.convertInternal(loggersDebug);

        // info
        assertThat(infoResponse.configuredLevel()).isNull();
        assertThat(infoResponse.effectiveLevel()).isEqualTo("INFO");

        // debug
        assertThat(debugResponse.configuredLevel()).isEqualTo("DEBUG");
        assertThat(debugResponse.effectiveLevel()).isEqualTo("DEBUG");
    }
}
