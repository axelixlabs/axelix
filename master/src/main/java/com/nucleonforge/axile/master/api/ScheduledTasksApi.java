package com.nucleonforge.axile.master.api;

import java.util.Objects;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.api.ServiceScheduledTasks;
import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.request.ScheduledTaskToggleRequest;
import com.nucleonforge.axile.master.api.response.ScheduledTasksResponse;
import com.nucleonforge.axile.master.service.convert.Converter;
import com.nucleonforge.axile.master.service.serde.JacksonMessageSerializationStrategy;
import com.nucleonforge.axile.master.service.transport.scheduled.DisableSingleScheduledTaskEndpointProber;
import com.nucleonforge.axile.master.service.transport.scheduled.EnableSingleScheduledTaskEndpointProber;
import com.nucleonforge.axile.master.service.transport.scheduled.GetAllScheduledTasksEndpointProber;

/**
 * The API for managing scheduled-tasks (i.e. those that are represented by {@link Scheduled @Scheduled} methods).
 *
 * @author Sergey Cherkasov
 */
@Tag(
        name = "ScheduledTasks API Controller",
        description = "The scheduled-tasks endpoint provides information about the application’s scheduled tasks.")
@RestController
@RequestMapping(path = ApiPaths.ScheduledTasksApi.MAIN)
public class ScheduledTasksApi {

    private final GetAllScheduledTasksEndpointProber getAllScheduledTasksEndpointProber;
    private final EnableSingleScheduledTaskEndpointProber enableSingleScheduledTaskEndpointProber;
    private final DisableSingleScheduledTaskEndpointProber disableSingleScheduledTaskEndpointProber;
    private final Converter<ServiceScheduledTasks, ScheduledTasksResponse> converter;
    private final JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy;

    public ScheduledTasksApi(
            GetAllScheduledTasksEndpointProber getAllScheduledTasksEndpointProber,
            EnableSingleScheduledTaskEndpointProber enableSingleScheduledTaskEndpointProber,
            DisableSingleScheduledTaskEndpointProber disableSingleScheduledTaskEndpointProber,
            Converter<ServiceScheduledTasks, ScheduledTasksResponse> converter,
            JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy) {
        this.getAllScheduledTasksEndpointProber = getAllScheduledTasksEndpointProber;
        this.enableSingleScheduledTaskEndpointProber = enableSingleScheduledTaskEndpointProber;
        this.disableSingleScheduledTaskEndpointProber = disableSingleScheduledTaskEndpointProber;
        this.converter = converter;
        this.jacksonMessageSerializationStrategy = jacksonMessageSerializationStrategy;
    }

    @Operation(
            summary = "Returns details of the application’s scheduled tasks",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
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
    public ScheduledTasksResponse getAllScheduledTasksResponse(@PathVariable("instanceId") String instanceId) {
        ServiceScheduledTasks serviceScheduledTasks =
                getAllScheduledTasksEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
        return Objects.requireNonNull(converter.convert(serviceScheduledTasks));
    }

    @Operation(
            summary =
                    "Allows enabling a scheduled task either according to its configured schedule or forcibly, ignoring the schedule.",
            responses = {
                @ApiResponse(description = "OK", responseCode = "200"),
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
    @PostMapping(path = ApiPaths.ScheduledTasksApi.ENABLE_TASK)
    public void enableSingleScheduledTask(
            @PathVariable("instanceId") String instanceId, @RequestBody ScheduledTaskToggleRequest request) {
        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        enableSingleScheduledTaskEndpointProber.invokeNoValue(InstanceId.of(instanceId), payload);
    }

    @Operation(
            summary =
                    "Allows disabling a scheduled task either according to its configured schedule or forcibly, ignoring the schedule.",
            responses = {
                @ApiResponse(description = "OK", responseCode = "200"),
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
    @PostMapping(path = ApiPaths.ScheduledTasksApi.DISABLE_TASK)
    public void disableSingleScheduledTask(
            @PathVariable("instanceId") String instanceId, @RequestBody ScheduledTaskToggleRequest request) {
        HttpPayload payload = HttpPayload.json(jacksonMessageSerializationStrategy.serialize(request));
        disableSingleScheduledTaskEndpointProber.invokeNoValue(InstanceId.of(instanceId), payload);
    }
}
