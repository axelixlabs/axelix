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

import com.nucleonforge.axile.common.api.info.ServiceInfo;
import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.response.info.InfoResponse;
import com.nucleonforge.axile.master.service.convert.Converter;
import com.nucleonforge.axile.master.service.transport.InfoEndpointProber;

/**
 * The API for managing info.
 *
 * @author Sergey Cherkasov
 */
@Tag(
        name = "General instance information API",
        description = "The info endpoint provides general information about the particular Spring Boot instance")
@RestController
@RequestMapping(path = ApiPaths.InfoApi.MAIN)
public class InfoApi {

    private final InfoEndpointProber infoEndpointProber;
    private final Converter<ServiceInfo, InfoResponse> converter;

    public InfoApi(InfoEndpointProber infoEndpointProber, Converter<ServiceInfo, InfoResponse> converter) {
        this.infoEndpointProber = infoEndpointProber;
        this.converter = converter;
    }

    @Operation(
            summary = "Returns general information about the instance.",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Info",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/info.html")
                        },
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = InfoResponse.class))),
                @ApiResponse(description = "Bad Request", responseCode = "400", content = @Content),
                @ApiResponse(description = "Internal Server Error", responseCode = "500", content = @Content)
            })
    @Parameter(name = "instanceId", description = "Application Instance ID", required = true)
    @GetMapping(path = ApiPaths.InfoApi.INSTANCE_ID)
    public InfoResponse getInfoResponse(@PathVariable("instanceId") String instanceId) {
        ServiceInfo serviceInfo = infoEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
        return Objects.requireNonNull(converter.convert(serviceInfo));
    }
}
