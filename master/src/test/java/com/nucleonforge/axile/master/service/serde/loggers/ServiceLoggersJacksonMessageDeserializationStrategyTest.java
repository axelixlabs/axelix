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
package com.nucleonforge.axile.master.service.serde.loggers;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.loggers.LoggerGroup;
import com.nucleonforge.axile.common.api.loggers.LoggerLevels;
import com.nucleonforge.axile.common.api.loggers.ServiceLoggers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ServiceLoggersJacksonMessageDeserializationStrategy}.
 *
 * @author Sergey Cherkasov
 */
public class ServiceLoggersJacksonMessageDeserializationStrategyTest {
    private final ServiceLoggersJacksonMessageDeserializationStrategy subject =
            new ServiceLoggersJacksonMessageDeserializationStrategy(new ObjectMapper());

    @Test
    void shouldDeserializeServiceLoggers() {
        // language=json
        String response =
                """

            {
          "levels" : [ "OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE" ],
          "loggers" : {
            "ROOT" : {
              "configuredLevel" : "INFO",
              "effectiveLevel" : "INFO"
            },
            "com.example" : {
              "configuredLevel" : "DEBUG",
              "effectiveLevel" : "DEBUG"
            },
            "org" : {
              "effectiveLevel" : "INFO"
            }
          },
          "groups" : {
            "test" : {
              "configuredLevel" : "INFO",
              "members" : [ "test.member1", "test.member2" ]
            },
            "web" : {
              "members" : [ "org.springframework.core.codec", "org.springframework.http", "org.springframework.web", "org.springframework.boot.actuate.endpoint.web", "org.springframework.boot.web.servlet.ServletContextInitializerBeans" ]
            },
            "sql" : {
              "members" : [ "org.springframework.jdbc.core", "org.hibernate.SQL", "org.jooq.tools.LoggerListener" ]
            }
          }
        }
        """;

        // when.
        ServiceLoggers loggers = subject.deserialize(response.getBytes(StandardCharsets.UTF_8));

        // levels
        assertThat(loggers.levels())
                .containsExactlyInAnyOrder("OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE");

        // logger
        Map<String, LoggerLevels> logger = loggers.loggers();
        assertThat(logger).hasSize(3);

        // logger -> "ROOT"
        assertThat(logger.get("ROOT").configuredLevel()).isEqualTo("INFO");
        assertThat(logger.get("ROOT").effectiveLevel()).isEqualTo("INFO");

        // logger -> "com.example"
        assertThat(logger.get("com.example").configuredLevel()).isEqualTo("DEBUG");
        assertThat(logger.get("com.example").effectiveLevel()).isEqualTo("DEBUG");

        // logger -> "org"
        assertThat(logger.get("org").configuredLevel()).isNull();
        assertThat(logger.get("org").effectiveLevel()).isEqualTo("INFO");

        // group
        Map<String, LoggerGroup> group = loggers.groups();
        assertThat(group).hasSize(3);

        // group -> "test"
        assertThat(group.get("test").configuredLevel()).isEqualTo("INFO");
        assertThat(group.get("test").members()).containsExactlyInAnyOrder("test.member1", "test.member2");

        // group -> "web"
        assertThat(group.get("web").configuredLevel()).isNull();
        assertThat(group.get("web").members())
                .containsExactlyInAnyOrder(
                        "org.springframework.core.codec",
                        "org.springframework.http",
                        "org.springframework.web",
                        "org.springframework.boot.actuate.endpoint.web",
                        "org.springframework.boot.web.servlet.ServletContextInitializerBeans");

        // group -> "sql"
        assertThat(group.get("sql").configuredLevel()).isNull();
        assertThat(group.get("sql").members())
                .containsExactlyInAnyOrder(
                        "org.springframework.jdbc.core", "org.hibernate.SQL", "org.jooq.tools.LoggerListener");
    }
}
