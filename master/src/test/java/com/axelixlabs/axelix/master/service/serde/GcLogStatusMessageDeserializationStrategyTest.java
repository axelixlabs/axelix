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
package com.axelixlabs.axelix.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.gclog.GcLogStatusResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link GcLogStatusMessageDeserializationStrategy}.
 *
 * @since 12.01.2025
 * @author Nikita Kirillov
 */
class GcLogStatusMessageDeserializationStrategyTest {

    private final GcLogStatusMessageDeserializationStrategy subject =
            new GcLogStatusMessageDeserializationStrategy(new ObjectMapper());

    @Test
    void shouldDeserializeGcLogStatus() {
        String STATUS_RESPONSE =
                // language=json
                """
            {
                "enabled": true,
                "level": "info",
                "availableLevels": [
                    "trace",
                    "debug",
                    "info",
                    "warning",
                    "error"
                ]
            }
            """;

        GcLogStatusResponse gcLogStatusResponse = subject.deserialize(STATUS_RESPONSE.getBytes());
        assertThat(gcLogStatusResponse).isNotNull();
        assertThat(gcLogStatusResponse.isEnabled()).isTrue();
        assertThat(gcLogStatusResponse.getLevel()).isEqualTo("info");
        assertThat(gcLogStatusResponse.getAvailableLevels()).containsOnly("info", "debug", "warning", "error", "trace");
    }
}
