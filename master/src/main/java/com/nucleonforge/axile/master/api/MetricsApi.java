package com.nucleonforge.axile.master.api;

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
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.api.metrics.MetricProfile;
import com.nucleonforge.axile.common.api.metrics.MetricsList;
import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.MultiValueQueryParameter;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.response.loggers.GroupProfileResponse;
import com.nucleonforge.axile.master.api.response.metrics.MetricsListResponse;
import com.nucleonforge.axile.master.api.response.metrics.SingleMetricProfileResponse;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.convert.Converter;
import com.nucleonforge.axile.master.service.transport.metrics.GetAllMetricsEndpointProber;
import com.nucleonforge.axile.master.service.transport.metrics.GetSingleMetricProfileEndpointProber;

/**
 * The API for managing metrics.
 *
 * @since 19.11.2025
 * @author Nikita Kirillov
 */
@Tag(name = "Metrics API Controller", description = "The endpoint that provides access to the metrics of the instances")
@RestController
@RequestMapping(path = ApiPaths.MetricsApi.MAIN)
public class MetricsApi {

    private final GetAllMetricsEndpointProber getAllMetricsEndpointProber;
    private final GetSingleMetricProfileEndpointProber getSingleMetricProfileEndpointProber;
    private final Converter<MetricsList, MetricsListResponse> allMetricsConverter;
    private final Converter<MetricProfile, SingleMetricProfileResponse> singleMetricConverter;

    public MetricsApi(
            GetAllMetricsEndpointProber getAllMetricsEndpointProber,
            GetSingleMetricProfileEndpointProber getSingleMetricProfileEndpointProber,
            Converter<MetricsList, MetricsListResponse> allMetricsConverter,
            Converter<MetricProfile, SingleMetricProfileResponse> singleMetricConverter) {
        this.getAllMetricsEndpointProber = getAllMetricsEndpointProber;
        this.getSingleMetricProfileEndpointProber = getSingleMetricProfileEndpointProber;
        this.allMetricsConverter = allMetricsConverter;
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
                                        schema = @Schema(implementation = GroupProfileResponse.class))),
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
    public MetricsListResponse getAllMetrics(@PathVariable("instanceId") String instanceId) {
        MetricsList metricsList = getAllMetricsEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
        return Objects.requireNonNull(allMetricsConverter.convert(metricsList));
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

        MetricProfile result = getSingleMetricProfileEndpointProber.invoke(
                InstanceId.of(instanceId),
                new DefaultHttpPayload(
                        List.of(new MultiValueQueryParameter("tag", tags)), Map.of("metric.name", metric)));

        return Objects.requireNonNull(singleMetricConverter.convert(result));
    }
}
