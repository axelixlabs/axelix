package com.nucleonforge.axile.master.api;

import java.util.Objects;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.api.AxileDetails;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.response.AxileDetailsResponse;
import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.convert.Converter;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;
import com.nucleonforge.axile.master.service.transport.DetailsEndpointProber;

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
    @Autowired
    private InstanceRegistry instanceRegistry;

    private final DetailsEndpointProber detailsEndpointProber;
    private final Converter<AxileDetails, AxileDetailsResponse> converter;

    public DetailsApi(
            InstanceRegistry instanceRegistry,
            DetailsEndpointProber detailsEndpointProber,
            Converter<AxileDetails, AxileDetailsResponse> converter) {
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
                                        schema = @Schema(implementation = AxileDetailsResponse.class))),
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
    public AxileDetailsResponse getDetailsResponse(@PathVariable("instanceId") String instanceId) {
        AxileDetails axileDetails = detailsEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);

        // TODO It is necessary to consider moving this to a separate service.
        Instance instance = instanceRegistry.get(InstanceId.of(instanceId)).orElse(null);
        String instanceName = instance == null ? "UNKNOW" : instance.name();

        return Objects.requireNonNull(converter.convert(axileDetails.setInstanceName(instanceName)));
    }
}
