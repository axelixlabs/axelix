package com.nucleonforge.axile.master.service.serde;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.ManagedServiceMetadata;

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

        ManagedServiceMetadata metadata = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        assertThat(metadata).isNotNull();
        assertThat(metadata.version()).isEqualTo("1.0.0-SNAPSHOT");
    }
}
