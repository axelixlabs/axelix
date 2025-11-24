package com.nucleonforge.axile.master.api;

import java.util.Objects;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.api.ConditionsFeed;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.response.ConditionsFeedResponse;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.convert.response.Converter;
import com.nucleonforge.axile.master.service.transport.ConditionsEndpointProber;

/**
 * The API for managing conditions.
 *
 * @since 16.10.2025
 * @author Nikita Kirillov
 */
@Tag(
        name = "Conditions API",
        description =
                "The conditions endpoint provides information about the application's auto-configuration conditions evaluation.")
@RestController
@RequestMapping(path = ApiPaths.ConditionsApi.MAIN)
public class ConditionsApi {

    private final ConditionsEndpointProber conditionsEndpointProber;
    private final Converter<ConditionsFeed, ConditionsFeedResponse> converter;

    public ConditionsApi(
            ConditionsEndpointProber conditionsEndpointProber,
            Converter<ConditionsFeed, ConditionsFeedResponse> converter) {
        this.conditionsEndpointProber = conditionsEndpointProber;
        this.converter = converter;
    }

    @Operation(
            summary = "Returns conditions feed for the given instance",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Conditions",
                                    description =
                                            "https://docs.spring.io/spring-boot/api/rest/actuator/conditions.html")
                        },
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ConditionsFeedResponse.class))),
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
    @GetMapping(path = ApiPaths.ConditionsApi.FEED)
    public ConditionsFeedResponse getConditionsFeed(@PathVariable("instanceId") String instanceId) {
        ConditionsFeed result = conditionsEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
        return Objects.requireNonNull(converter.convert(result));
    }
}
