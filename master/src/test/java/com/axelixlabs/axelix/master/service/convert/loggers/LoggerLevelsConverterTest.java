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

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.loggers.LoggerLevels;
import com.axelixlabs.axelix.master.api.external.response.loggers.LoggerProfileResponse;
import com.axelixlabs.axelix.master.service.convert.response.loggers.LoggerLevelsConverter;

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
