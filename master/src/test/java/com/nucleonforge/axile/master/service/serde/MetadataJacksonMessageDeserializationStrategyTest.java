package com.nucleonforge.axile.master.service.serde;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.AxileMetadata;

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
        // when.
        // language=json
        String response =
                """
    {
      "groupId": "com.nucleonforge.axile",
      "version": "1.0.0-SNAPSHOT"
     }
    """;

        AxileMetadata metadata = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        assertThat(metadata).isNotNull();
        assertThat(metadata.version()).isEqualTo("1.0.0-SNAPSHOT");
        assertThat(metadata.groupId()).isEqualTo("com.nucleonforge.axile");
    }
}
