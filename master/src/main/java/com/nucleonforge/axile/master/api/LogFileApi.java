package com.nucleonforge.axile.master.api;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.jspecify.annotations.Nullable;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpHeader;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.transport.LogFileEndpointProber;

/**
 * The API for logfile.
 *
 * @since 12.11.2025
 * @author Nikita Kirillov
 */
@RestController
@RequestMapping(path = ApiPaths.LogFileApi.MAIN)
public class LogFileApi {

    private final LogFileEndpointProber logFileEndpointProber;

    public LogFileApi(LogFileEndpointProber logFileEndpointProber) {
        this.logFileEndpointProber = logFileEndpointProber;
    }

    @Operation(
            summary = "Get log file for the given instance",
            description = "Returns log file as plain text. Supports Range header for partial content.",
            responses = {
                @ApiResponse(
                        description = "Log file content",
                        responseCode = "200",
                        content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
                @ApiResponse(
                        description = "Partial Content - when Range header is used",
                        responseCode = "206",
                        content = @Content(mediaType = "text/plain", schema = @Schema(type = "string")),
                        headers = {
                            @Header(
                                    name = "Content-Range",
                                    description = "Bytes range of the partial content",
                                    schema = @Schema(type = "string"))
                        }),
                @ApiResponse(
                        description = "Bad Request - instance not found",
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
                name = "Range",
                description = "Bytes range for partial content (e.g., 'bytes=0-1023')",
                in = ParameterIn.HEADER,
                schema = @Schema(type = "string"))
    })
    @GetMapping(path = ApiPaths.LogFileApi.INSTANCE_ID, produces = MediaType.TEXT_PLAIN_VALUE)
    public Resource getLogFile(
            @PathVariable("instanceId") String instanceId,
            @RequestHeader(value = "Range", required = false) @Nullable String rangeHeader) {

        List<HttpHeader> headers = new ArrayList<>();

        if (rangeHeader != null) {
            headers.add(new HttpHeader("Range", rangeHeader));
        }

        HttpPayload payload = new DefaultHttpPayload(headers);
        return logFileEndpointProber.invoke(InstanceId.of(instanceId), payload);
    }
}
