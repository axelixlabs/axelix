package com.nucleonforge.axile.master.api;

import java.util.Map;
import java.util.Objects;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.links.Link;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.api.AxileConfigPropsFeed;
import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.response.configprops.ConfigPropsFeedResponse;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.convert.response.Converter;
import com.nucleonforge.axile.master.service.transport.confogprops.ConfigPropsByPrefixEndpointProber;
import com.nucleonforge.axile.master.service.transport.confogprops.ConfigPropsEndpointProber;

/**
 * The API for managing configprops.
 *
 * @author Sergey Cherkasov
 */
@Tag(
        name = "Configprops API Controller",
        description =
                "The configprops endpoint provides information about the application’s @ConfigurationProperties beans.")
@RestController
@RequestMapping(path = ApiPaths.ConfigPropsApi.MAIN)
public class ConfigPropsApi {

    private final ConfigPropsEndpointProber configpropsEndpointProber;
    private final ConfigPropsByPrefixEndpointProber configpropsByPrefixEndpointProber;
    private final Converter<AxileConfigPropsFeed, ConfigPropsFeedResponse> configpropsFeedConverter;

    public ConfigPropsApi(
            ConfigPropsEndpointProber configpropsEndpointProber,
            ConfigPropsByPrefixEndpointProber configpropsByPrefixEndpointProber,
            Converter<AxileConfigPropsFeed, ConfigPropsFeedResponse> configpropsFeedConverter,
            Converter<AxileConfigPropsFeed, ConfigPropsFeedResponse> configpropsByPrefixResponseConverter) {
        this.configpropsEndpointProber = configpropsEndpointProber;
        this.configpropsByPrefixEndpointProber = configpropsByPrefixEndpointProber;
        this.configpropsFeedConverter = configpropsFeedConverter;
    }

    @Operation(
            summary = "Returns all @ConfigurationProperties beans of the application.",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Configuration Properties",
                                    description =
                                            "https://docs.spring.io/spring-boot/api/rest/actuator/configprops.html")
                        },
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ConfigPropsFeedResponse.class))),
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
    @GetMapping(path = ApiPaths.ConfigPropsApi.FEED)
    public ConfigPropsFeedResponse getConfigpropsFeed(@PathVariable("instanceId") String instanceId) {
        AxileConfigPropsFeed result =
                configpropsEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
        return Objects.requireNonNull(configpropsFeedConverter.convert(result));
    }

    @Operation(
            summary = "Returns the @ConfigurationProperties beans of a given instance with given prefix",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Configuration Properties",
                                    description =
                                            "https://docs.spring.io/spring-boot/api/rest/actuator/configprops.html")
                        },
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ConfigPropsFeedResponse.class))),
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
    @Parameters({
        @Parameter(name = "instanceId", description = "Application Instance ID", required = true),
        @Parameter(
                name = "prefix",
                description = "The prefix of @ConfigurationProperties beans to find",
                required = true)
    })
    @GetMapping(path = ApiPaths.ConfigPropsApi.BEAN_BY_PREFIX)
    public ConfigPropsFeedResponse getBeanByPrefixProfile(
            @PathVariable("instanceId") String instanceId, @PathVariable("prefix") String prefix) {
        HttpPayload payload = new DefaultHttpPayload(Map.of("prefix", prefix));
        AxileConfigPropsFeed result = configpropsByPrefixEndpointProber.invoke(InstanceId.of(instanceId), payload);
        return Objects.requireNonNull(configpropsFeedConverter.convert(result));
    }
}
