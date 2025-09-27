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
