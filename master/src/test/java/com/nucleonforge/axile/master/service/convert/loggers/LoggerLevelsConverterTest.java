package com.nucleonforge.axile.master.service.convert.loggers;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.loggers.LoggerLevels;
import com.nucleonforge.axile.master.api.response.loggers.LoggerProfileResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LoggerLevelsConverter}
 *
 * @author Sergey Cherkasov
 */
public class LoggerLevelsConverterTest {
    private final LoggerLevelsConverter subject = new LoggerLevelsConverter();

    @Test
    void testConvertHappyPath() {
        LoggerLevels loggersInfo = new LoggerLevels(null, "INFO");
        LoggerLevels loggersDebug = new LoggerLevels("DEBUG", "DEBUG");

        // when.
        LoggerProfileResponse infoResponse = subject.convertInternal(loggersInfo);
        LoggerProfileResponse debugResponse = subject.convertInternal(loggersDebug);

        // info
        assertThat(infoResponse.configuredLevel()).isNull();
        assertThat(infoResponse.effectiveLevel()).isEqualTo("INFO");

        // debug
        assertThat(debugResponse.configuredLevel()).isEqualTo("DEBUG");
        assertThat(debugResponse.effectiveLevel()).isEqualTo("DEBUG");
    }
}
