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

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.ProfileMutationResult;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ProfileMutationJacksonMessageDeserializationStrategy}.
 *
 * @since 24.09.2025
 * @author Nikita Kirillov
 */
class ProfileMutationJacksonMessageDeserializationStrategyTest {

    private final ProfileMutationJacksonMessageDeserializationStrategy subject =
            new ProfileMutationJacksonMessageDeserializationStrategy(new ObjectMapper());

    @Test
    void shouldDeserializeProfileMutationResult() {
        // language=json
        String jsonResponse = """
            {
              "updated": true,
              "reason": "New profiles have been activated"
            }
            """;

        // when.
        ProfileMutationResult feed = subject.deserialize(jsonResponse.getBytes(StandardCharsets.UTF_8));

        // then.
        assertThat(feed.updated()).isEqualTo(true);
        assertThat(feed.reason()).isEqualTo("New profiles have been activated");
    }
}
