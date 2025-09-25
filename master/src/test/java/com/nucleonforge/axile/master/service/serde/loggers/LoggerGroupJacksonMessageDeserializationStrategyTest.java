package com.nucleonforge.axile.master.service.serde.loggers;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.loggers.LoggerGroup;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LoggerGroupJacksonMessageDeserializationStrategy}.
 *
 * @author Sergey Cherkasov
 */
public class LoggerGroupJacksonMessageDeserializationStrategyTest {
    private final LoggerGroupJacksonMessageDeserializationStrategy subject =
            new LoggerGroupJacksonMessageDeserializationStrategy(new ObjectMapper());

    @Test
    void shouldDeserializeGroupLoggers() {
        // language=json
        String responseGroupTest =
                """
        {
          "configuredLevel" : "INFO",
          "members" : [ "test.member1", "test.member2" ]
        }
        """;

        // language=json
        String responseGroupSql =
                """
        {
          "members" : [ "org.springframework.jdbc.core", "org.hibernate.SQL", "org.jooq.tools.LoggerListener" ]
        }
        """;

        // when.
        LoggerGroup groupTest = subject.deserialize(responseGroupTest.getBytes(StandardCharsets.UTF_8));
        LoggerGroup groupSql = subject.deserialize(responseGroupSql.getBytes(StandardCharsets.UTF_8));

        // groupTest
        assertThat(groupTest.configuredLevel()).isEqualTo("INFO");
        assertThat(groupTest.members()).containsExactlyInAnyOrder("test.member1", "test.member2");

        // groupSql
        assertThat(groupSql.configuredLevel()).isNull();
        assertThat(groupSql.members())
                .containsExactlyInAnyOrder(
                        "org.springframework.jdbc.core", "org.hibernate.SQL", "org.jooq.tools.LoggerListener");
    }
}
