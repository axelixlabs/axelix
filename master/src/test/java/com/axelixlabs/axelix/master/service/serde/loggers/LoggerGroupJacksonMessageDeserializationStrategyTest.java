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
package com.axelixlabs.axelix.master.service.serde.loggers;

import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.loggers.LoggerGroup;

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
        String responseGroupTest = """
        {
          "configuredLevel" : "INFO",
          "members" : [ "test.member1", "test.member2" ]
        }
        """;

        // language=json
        String responseGroupSql = """
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
