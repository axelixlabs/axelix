package com.nucleonforge.axile.master.service.serde;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.caches.CacheDispatcherClearResult;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link CacheDispatcherJacksonMessageDeserializationStrategy}.
 *
 * @since 06.10.2025
 * @author Nikita Kirillov
 */
class CacheDispatcherJacksonMessageDeserializationStrategyTest {

    private final CacheDispatcherJacksonMessageDeserializationStrategy subject =
            new CacheDispatcherJacksonMessageDeserializationStrategy(new ObjectMapper());

    @Test
    void shouldDeserializeCacheDispatcherClearResult() {
        // language=json
        String jsonResponse = """
        {
          "cleared": true
        }
        """;

        // when.
        CacheDispatcherClearResult result = subject.deserialize(jsonResponse.getBytes(StandardCharsets.UTF_8));

        // then.
        assertThat(result.cleared()).isEqualTo(true);
    }
}
