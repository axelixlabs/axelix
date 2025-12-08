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
package com.nucleonforge.axile.master.service.convert.loggers;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.loggers.LoggerLevels;
import com.nucleonforge.axile.master.api.response.loggers.LoggerProfileResponse;
import com.nucleonforge.axile.master.service.convert.response.loggers.LoggerLevelsConverter;

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
