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
package com.nucleonforge.axile.master.service.serde;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.ProfileMutationResult;

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
        String jsonResponse =
                """
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
