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
package com.axelixlabs.axelix.master.mcp.tools;

import java.util.List;
import java.util.Map;

import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.master.mcp.McpEndpoints;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CacheMcpServerTools}.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
class CacheMcpServerToolsTest extends AbstractInstanceMcpServerToolsTest {

    @Test
    void shouldPreviewMatchesAndReportAnUnavailableInstance() throws InterruptedException {
        // given.
        String path = "/axelix-caches/cacheManager/owners/entries?pattern=owner%3A*";
        instance.respondWith(path, "3");
        anotherInstance.respondWithBadRequest(path);

        // when.
        String result = callTool(
                McpEndpoints.PREVIEW_CACHE_ENTRY_EVICTION_TOOL_NAME,
                Map.of(
                        "instanceIds", List.of(INSTANCE_ID, ANOTHER_INSTANCE_ID),
                        "cacheManager", "cacheManager",
                        "cacheName", "owners",
                        "pattern", "owner:*"));

        // then.
        assertThat(result).contains("estimatedMatches", INSTANCE_ID, "3", "failedInstanceIds", ANOTHER_INSTANCE_ID);
        assertThat(instance.takeRequest().getMethod()).isEqualTo("GET");
        assertThat(anotherInstance.takeRequest().getMethod()).isEqualTo("GET");
    }

    @Test
    void shouldSendLiteralEntryEvictionToEachInstance() throws InterruptedException {
        // when.
        String result = callTool(
                McpEndpoints.EVICT_CACHE_ENTRIES_TOOL_NAME,
                Map.of(
                        "instanceIds", List.of(INSTANCE_ID, ANOTHER_INSTANCE_ID),
                        "cacheManager", "cacheManager",
                        "cacheName", "owners",
                        "selector", "owner:*",
                        "wildcard", false));

        // then.
        assertThat(result).isEqualTo(DONE_RESPONSE);
        String path = "/axelix-caches/cacheManager/owners/entries?selector=owner%3A*&wildcard=false";
        assertCacheClearing(instance.takeRequest(), path);
        assertCacheClearing(anotherInstance.takeRequest(), path);
    }

    @Test
    void shouldFanTheSpecificCacheClearingOutToEveryListedInstance() throws InterruptedException {
        // when.
        String result = callTool(
                McpEndpoints.CLEAR_SPECIFIC_CACHE_TOOL_NAME,
                Map.of(
                        "instanceIds",
                        List.of(INSTANCE_ID, ANOTHER_INSTANCE_ID),
                        "cacheManager",
                        "cacheManager",
                        "cacheName",
                        "owners"));

        // then.
        assertThat(result).isEqualTo(DONE_RESPONSE);

        // and.
        assertCacheClearing(instance.takeRequest(), "/axelix-caches/cacheManager/owners/clear");
        assertCacheClearing(anotherInstance.takeRequest(), "/axelix-caches/cacheManager/owners/clear");
    }

    @Test
    void shouldFanTheClearingOfAllCachesOutToEveryListedInstance() throws InterruptedException {
        // when.
        String result = callTool(
                McpEndpoints.CLEAR_ALL_CACHES_TOOL_NAME,
                Map.of("instanceIds", List.of(INSTANCE_ID, ANOTHER_INSTANCE_ID)));

        // then.
        assertThat(result).isEqualTo(DONE_RESPONSE);

        // and.
        assertCacheClearing(instance.takeRequest(), "/axelix-caches/clear");
        assertCacheClearing(anotherInstance.takeRequest(), "/axelix-caches/clear");
    }

    @Test
    void shouldReportTheInstancesThatFailedToClearTheSpecificCache() throws InterruptedException {
        // given.
        anotherInstance.respondWithBadRequest("/axelix-caches/cacheManager/owners/clear");

        // when.
        String result = callToolExpectingError(
                McpEndpoints.CLEAR_SPECIFIC_CACHE_TOOL_NAME,
                Map.of(
                        "instanceIds",
                        List.of(INSTANCE_ID, ANOTHER_INSTANCE_ID),
                        "cacheManager",
                        "cacheManager",
                        "cacheName",
                        "owners"));

        // then. the error message format is produced by the Spring AI MCP server toolkit, not by Axelix.
        assertThat(result)
                .isEqualTo("Error invoking method: clearSpecificCacheEntity" + System.lineSeparator()
                        + "The operation has failed for the following instances: instance-2");

        // and. the instance that succeeded still has the cache cleared.
        assertCacheClearing(instance.takeRequest(), "/axelix-caches/cacheManager/owners/clear");
    }

    @Test
    void shouldReportTheInstancesThatFailedToClearAllCaches() throws InterruptedException {
        // given.
        anotherInstance.respondWithBadRequest("/axelix-caches/clear");

        // when.
        String result = callToolExpectingError(
                McpEndpoints.CLEAR_ALL_CACHES_TOOL_NAME,
                Map.of("instanceIds", List.of(INSTANCE_ID, ANOTHER_INSTANCE_ID)));

        // then. the error message format is produced by the Spring AI MCP server toolkit, not by Axelix.
        assertThat(result)
                .isEqualTo("Error invoking method: clearAllCaches" + System.lineSeparator()
                        + "The operation has failed for the following instances: instance-2");

        // and. the instance that succeeded still has the caches cleared.
        assertCacheClearing(instance.takeRequest(), "/axelix-caches/clear");
    }

    @Test
    void shouldRejectClearingTheSpecificCacheOnInstancesOfDifferentApplications() {
        // when.
        String result = callToolExpectingError(
                McpEndpoints.CLEAR_SPECIFIC_CACHE_TOOL_NAME,
                Map.of(
                        "instanceIds",
                        List.of(INSTANCE_ID, ANOTHER_APPLICATION_INSTANCE_ID),
                        "cacheManager",
                        "cacheManager",
                        "cacheName",
                        "owners"));

        // then. the error message format is produced by the Spring AI MCP server toolkit, not by Axelix.
        assertThat(result)
                .isEqualTo("Error invoking method: clearSpecificCacheEntity" + System.lineSeparator()
                        + "All instances must belong to the same application");

        // and.
        assertThat(instance.requestCount()).isZero();
    }

    @Test
    void shouldRejectClearingAllCachesOnInstancesOfDifferentApplications() {
        // when.
        String result = callToolExpectingError(
                McpEndpoints.CLEAR_ALL_CACHES_TOOL_NAME,
                Map.of("instanceIds", List.of(INSTANCE_ID, ANOTHER_APPLICATION_INSTANCE_ID)));

        // then. the error message format is produced by the Spring AI MCP server toolkit, not by Axelix.
        assertThat(result)
                .isEqualTo("Error invoking method: clearAllCaches" + System.lineSeparator()
                        + "All instances must belong to the same application");

        // and.
        assertThat(instance.requestCount()).isZero();
    }

    private static void assertCacheClearing(RecordedRequest request, String path) {
        assertThat(request.getMethod()).isEqualTo("DELETE");
        assertThat(request.getPath()).isEqualTo(ACTUATOR_PATH + path);
    }
}
