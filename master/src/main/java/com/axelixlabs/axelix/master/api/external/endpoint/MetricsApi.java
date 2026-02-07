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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.axelixlabs.axelix.common.api.metrics.MetricProfile;
import com.axelixlabs.axelix.common.api.metrics.MetricsGroupsFeed;
import com.axelixlabs.axelix.common.domain.http.DefaultHttpPayload;
import com.axelixlabs.axelix.common.domain.http.MultiValueQueryParameter;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.common.domain.http.QueryParameter;
import com.axelixlabs.axelix.master.api.error.SimpleApiError;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.response.metrics.MetricsGroupsFeedResponse;
import com.axelixlabs.axelix.master.api.external.response.metrics.SingleMetricProfileResponse;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.convert.response.Converter;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for managing metrics.
 *
 * @since 19.11.2025
 * @author Nikita Kirillov
 */
@Tag(name = "Metrics API Controller", description = "The endpoint that provides access to the metrics of the instances")
@ExternalApiRestController
@RequestMapping(path = ApiPaths.MetricsApi.MAIN)
public class MetricsApi {

    private final EndpointInvoker endpointInvoker;
    private final Converter<MetricsGroupsFeed, MetricsGroupsFeedResponse> metricsGroupsFeedConverter;
    private final Converter<MetricProfile, SingleMetricProfileResponse> singleMetricConverter;

    public MetricsApi(
            EndpointInvoker endpointInvoker,
            Converter<MetricsGroupsFeed, MetricsGroupsFeedResponse> metricsGroupsFeedConverter,
            Converter<MetricProfile, SingleMetricProfileResponse> singleMetricConverter) {
        this.endpointInvoker = endpointInvoker;
        this.metricsGroupsFeedConverter = metricsGroupsFeedConverter;
        this.singleMetricConverter = singleMetricConverter;
    }

    @Operation(
            summary = "Returns all possible metrics that exists inside the given instance",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Metrics",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/metrics.html")
                        },
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = MetricsGroupsFeedResponse.class))),
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
    @GetMapping(path = ApiPaths.MetricsApi.INSTANCE_ID)
    public MetricsGroupsFeedResponse getMetricGroups(@PathVariable("instanceId") String instanceId) {
        MetricsGroupsFeed metricsList = endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_METRIC_GROUPS, NoHttpPayload.INSTANCE);
        return Objects.requireNonNull(metricsGroupsFeedConverter.convert(metricsList));
    }

    @Operation(
            summary = "Returns a single metric profile inside the given instance",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Metrics",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/metrics.html")
                        },
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SingleMetricProfileResponse.class))),
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
    @Parameter(name = "metric", description = "The name of the metric to fetch profile for", required = true)
    @Parameter(
            name = "tag",
            description = "Tag to filter the metric by. Multiple tags can be provided. Format: key:value",
            array = @ArraySchema(schema = @Schema(type = "string", example = "area:nonheap")))
    @GetMapping(path = ApiPaths.MetricsApi.METRIC_NAME)
    public SingleMetricProfileResponse getSingleMetric(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("metric") String metric,
            @RequestParam(value = "tag", required = false) List<String> tags) {

        List<QueryParameter<?>> queryParameters = new ArrayList<>();
        if (tags != null && !tags.isEmpty()) {
            queryParameters.add(new MultiValueQueryParameter("tag", tags));
        }

        MetricProfile result = endpointInvoker.invoke(
                InstanceId.of(instanceId),
                ActuatorEndpoints.GET_SINGLE_METRIC,
                new DefaultHttpPayload(queryParameters, Map.of("metric.name", metric)));

        return Objects.requireNonNull(singleMetricConverter.convert(result));
    }
}
