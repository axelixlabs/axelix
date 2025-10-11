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

import com.nucleonforge.axile.common.api.ServiceScheduledTasks;
import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.response.ScheduledTasksResponse;
import com.nucleonforge.axile.master.service.convert.Converter;
import com.nucleonforge.axile.master.service.transport.ScheduledTasksEndpointProber;

/**
 * The API for managing scheduledtasks.
 *
 * @author Sergey Cherkasov
 */
@Tag(
        name = "ScheduledTasks API Controller",
        description = "The scheduledtasks endpoint provides information about the application’s scheduled tasks.")
@RestController
@RequestMapping(path = ApiPaths.ScheduledTasksApi.MAIN)
public class ScheduledTasksApi {

    private final ScheduledTasksEndpointProber scheduledTasksEndpointProber;
    private final Converter<ServiceScheduledTasks, ScheduledTasksResponse> converter;

    public ScheduledTasksApi(
            ScheduledTasksEndpointProber scheduledTasksEndpointProber,
            Converter<ServiceScheduledTasks, ScheduledTasksResponse> converter) {
        this.scheduledTasksEndpointProber = scheduledTasksEndpointProber;
        this.converter = converter;
    }

    @Operation(
            summary = "Returns details of the application’s scheduled tasks",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/ScheduledTasks",
                                    description =
                                            "https://docs.spring.io/spring-boot/api/rest/actuator/scheduledtasks.html")
                        },
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ScheduledTasksResponse.class))),
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
    @GetMapping(path = ApiPaths.ScheduledTasksApi.INSTANCE_ID)
    public ScheduledTasksResponse getScheduledTasksFeedResponse(@PathVariable("instanceId") String instanceId) {
        ServiceScheduledTasks serviceScheduledTasks =
                scheduledTasksEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
        return Objects.requireNonNull(converter.convert(serviceScheduledTasks));
    }
}
