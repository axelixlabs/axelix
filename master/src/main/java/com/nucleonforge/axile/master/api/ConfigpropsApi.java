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

import com.nucleonforge.axile.common.api.ConfigpropsFeed;
import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.response.configprops.ConfigpropsByPrefixResponse;
import com.nucleonforge.axile.master.api.response.configprops.ConfigpropsFeedResponse;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.convert.Converter;
import com.nucleonforge.axile.master.service.transport.confogprops.ConfigpropsByPrefixEndpointProber;
import com.nucleonforge.axile.master.service.transport.confogprops.ConfigpropsEndpointProber;

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
@RequestMapping(path = ApiPaths.ConfigpropsApi.MAIN)
public class ConfigpropsApi {

    private final ConfigpropsEndpointProber configpropsEndpointProber;
    private final ConfigpropsByPrefixEndpointProber configpropsByPrefixEndpointProber;
    private final Converter<ConfigpropsFeed, ConfigpropsFeedResponse> configpropsFeedConverter;
    private final Converter<ConfigpropsFeed, ConfigpropsByPrefixResponse> configpropsByPrefixResponseConverter;

    public ConfigpropsApi(
            ConfigpropsEndpointProber configpropsEndpointProber,
            ConfigpropsByPrefixEndpointProber configpropsByPrefixEndpointProber,
            Converter<ConfigpropsFeed, ConfigpropsFeedResponse> configpropsFeedConverter,
            Converter<ConfigpropsFeed, ConfigpropsByPrefixResponse> configpropsByPrefixResponseConverter) {
        this.configpropsEndpointProber = configpropsEndpointProber;
        this.configpropsByPrefixEndpointProber = configpropsByPrefixEndpointProber;
        this.configpropsFeedConverter = configpropsFeedConverter;
        this.configpropsByPrefixResponseConverter = configpropsByPrefixResponseConverter;
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
                                        schema = @Schema(implementation = ConfigpropsFeedResponse.class))),
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
    @GetMapping(path = ApiPaths.ConfigpropsApi.FEED)
    public ConfigpropsFeedResponse getConfigpropsFeed(@PathVariable("instanceId") String instanceId) {
        ConfigpropsFeed result = configpropsEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
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
                                        schema = @Schema(implementation = ConfigpropsByPrefixResponse.class))),
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
    @GetMapping(path = ApiPaths.ConfigpropsApi.BEAN_BY_PREFIX)
    public ConfigpropsByPrefixResponse getBeanByPrefixProfile(
            @PathVariable("instanceId") String instanceId, @PathVariable("prefix") String prefix) {
        HttpPayload payload = new DefaultHttpPayload(Map.of("prefix", prefix));
        ConfigpropsFeed result = configpropsByPrefixEndpointProber.invoke(InstanceId.of(instanceId), payload);
        return Objects.requireNonNull(configpropsByPrefixResponseConverter.convert(result));
    }
}
