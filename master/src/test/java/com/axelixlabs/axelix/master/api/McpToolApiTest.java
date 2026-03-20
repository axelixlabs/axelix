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
package com.axelixlabs.axelix.master.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.master.ApplicationEntrypoint;
import com.axelixlabs.axelix.master.api.external.endpoint.McpToolApi;
import com.axelixlabs.axelix.master.utils.InvalidAuthScenario;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_ARRAY_ITEMS;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link McpToolApi}.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class McpToolApiTest {

    @Autowired
    private TestRestTemplateBuilder restTemplate;

    // language=json
    private static final String EXPECTED_MCP_TOOLS_FEED = """
        {
          "tools" : [ {
            "title" : "Beans Feed",
            "description" : "List of all the beans available in the ApplicationContext",
            "annotations" : {
              "readOnlyHint" : true,
              "destructiveHint" : false,
              "idempotentHint" : true,
              "openWorldHint" : false
            },
            "status" : "UP"
          }, {
            "title" : "@Conditional Feed",
            "description" : "List that contain results of all @Conditional evaluations",
            "annotations" : {
              "readOnlyHint" : true,
              "destructiveHint" : false,
              "idempotentHint" : true,
              "openWorldHint" : false
            },
            "status" : "UP"
          }, {
            "title" : "Config Props Beans",
            "description" : "List of all the @ConfigurationProperties beans inside this Spring Boot app",
            "annotations" : {
              "readOnlyHint" : true,
              "destructiveHint" : false,
              "idempotentHint" : true,
              "openWorldHint" : false
            },
            "status" : "UP"
          }, {
            "title" : "Properties",
            "description" : "List of all the properties inside the Spring Boot application",
            "annotations" : {
              "readOnlyHint" : true,
              "destructiveHint" : false,
              "idempotentHint" : true,
              "openWorldHint" : false
            },
            "status" : "UP"
          }, {
            "title" : "Scheduled Tasks",
            "description" : "List of all scheduled tasks inside this Spring Boot app",
            "annotations" : {
              "readOnlyHint" : true,
              "destructiveHint" : false,
              "idempotentHint" : true,
              "openWorldHint" : false
            },
            "status" : "UP"
          }, {
            "title" : "Instances Feed",
            "description" : "List of all Spring Boot applications instances currently deployed",
            "annotations" : {
              "readOnlyHint" : true,
              "destructiveHint" : false,
              "idempotentHint" : true,
              "openWorldHint" : false
            },
            "status" : "UP"
          } ]
        }
        """;

    @Test
    void shouldReturnMcpToolsFeed() {
        // when.
        ResponseEntity<String> response =
                restTemplate.withoutAuthorities().getForEntity("/api/external/mcp/tools-feed", String.class);

        // then.
        assertThatJson(response.getBody())
                .when(IGNORING_EXTRA_ARRAY_ITEMS)
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(EXPECTED_MCP_TOOLS_FEED);
    }

    @ParameterizedTest
    @EnumSource(InvalidAuthScenario.class)
    void shouldReturnUnauthorized(InvalidAuthScenario scenario) {
        // when.
        ResponseEntity<Void> response =
                scenario.getModifier().apply(restTemplate).getForEntity("/api/external/mcp/tools-feed", Void.class);

        // then.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
