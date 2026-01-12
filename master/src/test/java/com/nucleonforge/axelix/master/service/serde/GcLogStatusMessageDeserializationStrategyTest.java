/*
 * Copyright 2025-present, Nucleon Forge Software.
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
package com.nucleonforge.axelix.master.service.serde;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axelix.common.api.gclog.GcLogStatusResponse;

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
        assertThat(gcLogStatusResponse.enabled()).isTrue();
        assertThat(gcLogStatusResponse.level()).isEqualTo("info");
        assertThat(gcLogStatusResponse.availableLevels()).containsOnly("info", "debug", "warning", "error", "trace");
    }
}
