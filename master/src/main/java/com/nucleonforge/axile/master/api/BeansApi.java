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

import com.nucleonforge.axile.common.api.BeansFeed;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.response.BeansFeedResponse;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.convert.response.Converter;
import com.nucleonforge.axile.master.service.transport.BeansEndpointProber;

/**
 * The API for managing beans.
 *
 * @author Mikhail Polivakha
 */
@Tag(name = "Beans API", description = "The beans endpoint provides information about the application’s beans.")
@RestController
@RequestMapping(path = ApiPaths.BeansApi.MAIN)
public class BeansApi {

    private final BeansEndpointProber beansEndpointProber;
    private final Converter<BeansFeed, BeansFeedResponse> converter;

    public BeansApi(BeansEndpointProber beansEndpointProber, Converter<BeansFeed, BeansFeedResponse> converter) {
        this.beansEndpointProber = beansEndpointProber;
        this.converter = converter;
    }

    @Operation(
            summary = "Returns beans feed for the given instance",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Beans",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/beans.html")
                        },
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = BeansFeedResponse.class))),
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
    @GetMapping(path = ApiPaths.BeansApi.FEED)
    public BeansFeedResponse getBeansProfile(@PathVariable("instanceId") String instanceId) {
        BeansFeed result = beansEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
        return Objects.requireNonNull(converter.convert(result));
    }
}
