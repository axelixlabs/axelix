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
package com.nucleonforge.axelix.master.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axelix.master.api.error.SimpleApiError;
import com.nucleonforge.axelix.master.api.response.DashboardResponse;
import com.nucleonforge.axelix.master.service.DashboardService;

/**
 * API for rendering the dashboard.
 *
 * @author Mikhail Polivakha
 */
// TODO: Cover with tests
@Tag(name = "Dashboard API", description = "API for rendering the dashboard")
@RestController
@RequestMapping(path = ApiPaths.DashboardApi.MAIN)
public class DashboardApi {

    private final DashboardService dashboardService;

    public DashboardApi(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

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
    @GetMapping
    public DashboardResponse getDashboard() {
        return dashboardService.getDashboardInfo();
    }
}
