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
package com.axelixlabs.axelix.e2e.client;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Client for Axelix Master's MCP endpoint {@code /api/mcp}.
 *
 * @author Nikita Kirillov
 */
public class McpClient {

    private static final String MCP_PATH = "/api/mcp";
    private static final String ACCEPT_HEADER = "text/event-stream, application/json";

    private final String baseUrl;
    private final String username;
    private final String password;

    public McpClient(String baseUrl, String username, String password) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
    }

    /**
     * Performs the MCP {@code initialize} handshake and returns the {@code Mcp-Session-Id} the
     * server assigned, to be passed to every subsequent call ({@link #listTools}).
     */
    public String initializeSession() {
        Response response = request()
                .body(
                        // language=json
                        """
                {
                  "jsonrpc":"2.0",
                  "id":1,
                  "method":"initialize",
                  "params":{
                    "protocolVersion":"2025-11-25",
                    "capabilities":{},
                    "clientInfo":{
                      "name":"axelix-e2e",
                      "version":"1.0.0"
                    }
                  }
                }
                """)
                .post(MCP_PATH)
                .then()
                .statusCode(200)
                .extract()
                .response();

        String sessionId = response.getHeader("Mcp-Session-Id");
        assertThat(sessionId)
                .as("Mcp-Session-Id response header from MCP initialize")
                .isNotBlank();
        return sessionId;
    }

    /**
     * Calls {@code tools/list} on an already-{@link #initializeSession() initialized} session.
     */
    public JsonPath listTools(String sessionId) {
        Response response = request()
                .header("Mcp-Session-Id", sessionId)
                .body(
                        // language=json
                        """
                {
                  "jsonrpc":"2.0",
                  "id":2,
                  "method":"tools/list",
                  "params":{}
                }
                """)
                .post(MCP_PATH)
                .then()
                .statusCode(200)
                .extract()
                .response();

        return jsonRpcResult(response);
    }

    private RequestSpecification request() {
        return given().baseUri(baseUrl)
                .auth()
                .preemptive()
                .basic(username, password)
                .contentType("application/json")
                .header("Accept", ACCEPT_HEADER);
    }

    private JsonPath jsonRpcResult(Response response) {
        String contentType = response.contentType();
        if (contentType != null && contentType.contains("text/event-stream")) {
            String body = response.getBody().asString();
            String dataLine = body.lines()
                    .filter(line -> line.startsWith("data:"))
                    .map(line -> line.substring("data:".length()).trim())
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No SSE 'data:' frame found in MCP response body: " + body));
            return new JsonPath(dataLine);
        }
        return response.jsonPath();
    }
}
