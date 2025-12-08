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

import java.util.Set;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.response.InstancesGridResponse;
import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.service.convert.response.InstancesToShortProfileConverter;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * The API for managing applications.
 *
 * @since 19.07.2025
 * @author Mikhail Polivakha
 */
@Tag(name = "Wallboard API", description = "The endpoints related to wallboard grid rendering")
@RestController
@RequestMapping(path = ApiPaths.InstancesApi.MAIN)
public class WallboardApi {

    private final InstanceRegistry instanceRegistry;
    private final InstancesToShortProfileConverter instancesToShortProfileConverter;

    public WallboardApi(
            InstanceRegistry instanceRegistry, InstancesToShortProfileConverter instancesToShortProfileConverter) {
        this.instanceRegistry = instanceRegistry;
        this.instancesToShortProfileConverter = instancesToShortProfileConverter;
    }

    @Operation(
            summary = "Returns all instances that are managed by this Axile deployment",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = InstancesGridResponse.class))),
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
    @GetMapping(path = ApiPaths.InstancesApi.GRID)
    @SuppressWarnings("NullAway")
    public InstancesGridResponse getInstancesGrid() {
        Set<Instance> all = instanceRegistry.getAll();
        return new InstancesGridResponse(instancesToShortProfileConverter.convertAll(all));
    }
}
