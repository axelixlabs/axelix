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
package com.nucleonforge.axile.master.api;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.response.DashboardResponse;
import com.nucleonforge.axile.master.api.response.software.DistributionResponse;

/**
 * API for rendering the dashboard.
 *
 * @author Mikhail Polivakha
 */
@Tag(name = "Dashboard API", description = "API for rendering the dashboard")
@RestController
@RequestMapping(path = ApiPaths.DashboardApi.MAIN)
public class DashboardApi {

    @Operation(
            summary = "Retrieve information about the entire ecosystem to render the dashboard",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = DashboardResponse.class))),
                @ApiResponse(
                        description = "Bad Request",
                        responseCode = "400",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class))),
                @ApiResponse(
                        description = "Internal Server Error",
                        responseCode = "500",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class)))
            })
    @GetMapping(path = ApiPaths.DashboardApi.MAIN)
    public DashboardResponse getDashboard() {
        return new DashboardResponse(
                List.of(
                        new DistributionResponse("Java", Map.of("17", 35L, "21", 151L, "25", 14L)),
                        new DistributionResponse("Kotlin", Map.of("1.8", 2L, "2.0", 7L, "2.2", 1L)),
                        new DistributionResponse("JdkVendor", Map.of("Azul", 35L, "Liberica", 165L)),
                        new DistributionResponse("SpringBoot", Map.of("3.2", 35L, "3.3", 12L, "3.4", 4L, "4.0", 76L)),
                        new DistributionResponse(
                                "SpringFramework", Map.of("6.1", 21L, "6.2", 19L, "6.3", 7L, "7.0", 2L))),
                new DashboardResponse.HealthStatus(Map.of(
                        DashboardResponse.Status.DOWN, 2,
                        DashboardResponse.Status.UP, 18,
                        DashboardResponse.Status.UNKNOWN, 1)),
                new DashboardResponse.MemoryUsageMap(
                        new DashboardResponse.MemoryUsage("MB", 477.2),
                        new DashboardResponse.MemoryUsage("MB", 1231322.1)));
    }
}
