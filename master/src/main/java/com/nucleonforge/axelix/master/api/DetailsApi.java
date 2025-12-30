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

import java.util.Objects;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axelix.common.api.InstanceDetails;
import com.nucleonforge.axelix.common.domain.http.NoHttpPayload;
import com.nucleonforge.axelix.master.api.error.SimpleApiError;
import com.nucleonforge.axelix.master.api.response.InstanceDetailsResponse;
import com.nucleonforge.axelix.master.exception.InstanceNotFoundException;
import com.nucleonforge.axelix.master.model.instance.InstanceId;
import com.nucleonforge.axelix.master.service.convert.response.Converter;
import com.nucleonforge.axelix.master.service.convert.response.details.DetailsConversionRequest;
import com.nucleonforge.axelix.master.service.transport.DetailsEndpointProber;

/**
 * The API for managing details.
 *
 * @author Nikita Kirilov, Sergey Cherkasov
 */
@Tag(
        name = "Details API",
        description = "The details endpoint provides general information about the particular Spring Boot instance")
@RestController
@RequestMapping(path = ApiPaths.DetailsApi.MAIN)
public class DetailsApi {

    private final DetailsEndpointProber detailsEndpointProber;
    private final Converter<DetailsConversionRequest, InstanceDetailsResponse> converter;

    public DetailsApi(
            DetailsEndpointProber detailsEndpointProber,
            Converter<DetailsConversionRequest, InstanceDetailsResponse> converter) {
        this.detailsEndpointProber = detailsEndpointProber;
        this.converter = converter;
    }

    @Operation(
            summary = "Returns general information about the instance.",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = InstanceDetailsResponse.class))),
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
    @Parameter(name = "instanceId", description = "Application Instance ID", required = true)
    @GetMapping(path = ApiPaths.DetailsApi.INSTANCE_ID)
    public InstanceDetailsResponse getDetailsResponse(@PathVariable("instanceId") String instanceId)
            throws InstanceNotFoundException {

        InstanceId id = InstanceId.of(instanceId);
        InstanceDetails instanceDetails = detailsEndpointProber.invoke(id, NoHttpPayload.INSTANCE);
        return Objects.requireNonNull(converter.convert(new DetailsConversionRequest(instanceDetails, id)));
    }
}
