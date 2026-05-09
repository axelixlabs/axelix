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
package com.axelixlabs.axelix.master.api.external.endpoint;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.master.ApplicationEntrypoint;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;
import com.axelixlabs.axelix.master.utils.auth.ProtectedEndpointTests;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_ARRAY_ORDER;
import static net.javacrumbs.jsonunit.core.Option.IGNORING_EXTRA_ARRAY_ITEMS;

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
              "tools" : [
                  {
                    "title" : "Clear All Cache",
                    "description" : "Clears all caches inside the Spring Boot app",
                    "annotations" : {
                      "readOnlyHint" : false,
                      "destructiveHint" : true,
                      "idempotentHint" : true,
                      "openWorldHint" : false
                    },
                    "status" : "UP"
                  },
                  {
                    "title" : "Clear Specific Cache",
                    "description" : "Clears a specific cache inside a given Cache Manager",
                    "annotations" : {
                      "readOnlyHint" : false,
                      "destructiveHint" : true,
                      "idempotentHint" : true,
                      "openWorldHint" : false
                    },
                    "status" : "UP"
                  },
                  {
                    "title" : "Caches Feed",
                    "description" : "Provides the list of all caches (along with Cache Managers) inside this Spring Boot app",
                    "annotations" : {
                      "readOnlyHint" : true,
                      "destructiveHint" : false,
                      "idempotentHint" : true,
                      "openWorldHint" : false
                    },
                    "status" : "UP"
                  },
                  {
                    "title" : "Beans Feed",
                    "description" : "List of all the beans available in the ApplicationContext",
                    "annotations" : {
                      "readOnlyHint" : true,
                      "destructiveHint" : false,
                      "idempotentHint" : true,
                      "openWorldHint" : false
                    },
                    "status" : "UP"
                  },
                  {
                    "title" : "@Conditional Feed",
                    "description" : "List that contain results of all @Conditional evaluations",
                    "annotations" : {
                      "readOnlyHint" : true,
                      "destructiveHint" : false,
                      "idempotentHint" : true,
                      "openWorldHint" : false
                    },
                    "status" : "UP"
                  },
                  {
                    "title" : "Config Props Beans",
                    "description" : "List of all the @ConfigurationProperties beans inside this Spring Boot app",
                    "annotations" : {
                      "readOnlyHint" : true,
                      "destructiveHint" : false,
                      "idempotentHint" : true,
                      "openWorldHint" : false
                    },
                    "status" : "UP"
                  },
                  {
                    "title" : "Properties",
                    "description" : "List of all the properties inside the Spring Boot application",
                    "annotations" : {
                      "readOnlyHint" : true,
                      "destructiveHint" : false,
                      "idempotentHint" : true,
                      "openWorldHint" : false
                    },
                    "status" : "UP"
                  },
                  {
                    "title" : "Scheduled Tasks",
                    "description" : "List of all scheduled tasks inside this Spring Boot app",
                    "annotations" : {
                      "readOnlyHint" : true,
                      "destructiveHint" : false,
                      "idempotentHint" : true,
                      "openWorldHint" : false
                    },
                    "status" : "UP"
                  },
                  {
                    "title" : "Instances Feed",
                    "description" : "List of all Spring Boot applications instances currently deployed",
                    "annotations" : {
                      "readOnlyHint" : true,
                      "destructiveHint" : false,
                      "idempotentHint" : true,
                      "openWorldHint" : false
                    },
                    "status" : "UP"
                  }
              ]
            }
            """;

    @Test
    void shouldReturnMcpToolsFeed() {
        // when.
        ResponseEntity<String> response =
                restTemplate.asViewer().getForEntity("/api/external/mcp/tools-feed", String.class);

        // then.
        assertThatJson(response.getBody())
                .when(IGNORING_EXTRA_ARRAY_ITEMS)
                .when(IGNORING_ARRAY_ORDER)
                .isEqualTo(EXPECTED_MCP_TOOLS_FEED);
    }

    @ProtectedEndpointTests(method = HttpMethod.GET, path = "/api/external/mcp/tools-feed")
    void negativeAuthTests() {}
}
