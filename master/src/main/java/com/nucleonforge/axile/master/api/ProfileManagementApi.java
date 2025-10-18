package com.nucleonforge.axile.master.api;

import java.util.Objects;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.api.ProfileMutationResult;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.request.ProfileUpdatedRequest;
import com.nucleonforge.axile.master.api.response.ProfileUpdateResponse;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.convert.Converter;
import com.nucleonforge.axile.master.service.serde.MessageSerializationStrategy;
import com.nucleonforge.axile.master.service.transport.ProfileManagementEndpointProber;

/**
 * The API for managing profiles.
 *
 * @since 24.09.2025
 * @author Nikita Kirillov
 */
@Tag(
        name = "Profile Management API",
        description = "Provides operations for managing Spring profiles of application instances.")
@RestController
@RequestMapping(path = ApiPaths.ProfileManagementApi.MAIN)
public class ProfileManagementApi {

    private final ProfileManagementEndpointProber profileManagementEndpointProber;
    private final Converter<ProfileMutationResult, ProfileUpdateResponse> converter;
    private final MessageSerializationStrategy messageSerializationStrategy;

    public ProfileManagementApi(
            ProfileManagementEndpointProber profileManagementEndpointProber,
            Converter<ProfileMutationResult, ProfileUpdateResponse> converter,
            MessageSerializationStrategy messageSerializationStrategy) {
        this.profileManagementEndpointProber = profileManagementEndpointProber;
        this.converter = converter;
        this.messageSerializationStrategy = messageSerializationStrategy;
    }

    @Operation(
            summary = "Replaces active profiles for the given application instance.",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = ProfileUpdateResponse.class))),
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
    @Parameters(@Parameter(name = "instanceId", description = "Application Instance ID", required = true))
    @PostMapping(path = ApiPaths.ProfileManagementApi.INSTANCE_ID)
    public ProfileUpdateResponse replaceProfile(
            @PathVariable("instanceId") String instanceId, @RequestBody ProfileUpdatedRequest request) {

        HttpPayload payload = HttpPayload.json(messageSerializationStrategy.serialize(request));
        ProfileMutationResult result = profileManagementEndpointProber.invoke(InstanceId.of(instanceId), payload);
        return Objects.requireNonNull(converter.convert(result));
    }
}
