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

import com.nucleonforge.axile.common.api.registration.ServiceMetadata;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MetadataJacksonMessageDeserializationStrategy}.
 *
 * @author Nikita Kirillov
 */
class MetadataJacksonMessageDeserializationStrategyTest {

    private final MetadataJacksonMessageDeserializationStrategy subject =
            new MetadataJacksonMessageDeserializationStrategy(new ObjectMapper());

    @Test
    void shouldDeserializeMetadata() {
        // language=json
        String response = """
           {
             "version": "1.0.0-SNAPSHOT"
           }
           """;

        ServiceMetadata metadata = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        assertThat(metadata).isNotNull();
        assertThat(metadata.version()).isEqualTo("1.0.0-SNAPSHOT");
    }
}
