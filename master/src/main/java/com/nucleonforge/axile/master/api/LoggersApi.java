package com.nucleonforge.axile.master.api;

import java.util.Collections;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.api.loggers.LoggerGroup;
import com.nucleonforge.axile.common.api.loggers.LoggerLevels;
import com.nucleonforge.axile.common.api.loggers.ServiceLoggers;
import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.request.LogLevelChangeRequest;
import com.nucleonforge.axile.master.api.response.loggers.GroupProfileResponse;
import com.nucleonforge.axile.master.api.response.loggers.LoggerProfileResponse;
import com.nucleonforge.axile.master.api.response.loggers.LoggersResponse;
import com.nucleonforge.axile.master.service.convert.Converter;
import com.nucleonforge.axile.master.service.serde.JacksonMessageSerializationStrategy;
import com.nucleonforge.axile.master.service.transport.loggers.AllLoggersEndpointProber;
import com.nucleonforge.axile.master.service.transport.loggers.ClearForLoggerEndpointProber;
import com.nucleonforge.axile.master.service.transport.loggers.GroupLoggersEndpointProber;
import com.nucleonforge.axile.master.service.transport.loggers.OneLoggerEndpointProber;
import com.nucleonforge.axile.master.service.transport.loggers.SetForLoggerGroupEndpointProber;
import com.nucleonforge.axile.master.service.transport.loggers.SetOneLoggerEndpointProber;

/**
 * The API for managing loggers.
 *
 * @author Sergey Cherkasov
 */
@Tag(
        name = "Loggers API Controller",
        description =
                "The loggers endpoint provides access to the application’s loggers and the configuration of their levels.")
@RestController
@RequestMapping(path = ApiPaths.LoggersApi.MAIN)
public class LoggersApi {

    private final AllLoggersEndpointProber allLoggersEndpointProber;
    private final GroupLoggersEndpointProber groupLoggersEndpointProber;
    private final OneLoggerEndpointProber oneLoggerEndpointProber;
    private final SetOneLoggerEndpointProber setOneLoggerEndpointProber;
    private final SetForLoggerGroupEndpointProber setForLoggerGroupEndpointProber;
    private final ClearForLoggerEndpointProber clearForLoggerEndpointProber;
    private final Converter<ServiceLoggers, LoggersResponse> loggersResponseConverter;
    private final Converter<LoggerGroup, GroupProfileResponse> groupProfileConverter;
    private final Converter<LoggerLevels, LoggerProfileResponse> loggerProfileConverter;
    private final JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy;

    public LoggersApi(
            AllLoggersEndpointProber allLoggersEndpointProber,
            GroupLoggersEndpointProber groupLoggersEndpointProber,
            OneLoggerEndpointProber oneLoggerEndpointProber,
            SetOneLoggerEndpointProber setOneLoggerEndpointProber,
            SetForLoggerGroupEndpointProber setForLoggerGroupEndpointProber,
            ClearForLoggerEndpointProber clearForLoggerEndpointProber,
            Converter<ServiceLoggers, LoggersResponse> loggersResponseConverter,
            Converter<LoggerGroup, GroupProfileResponse> groupProfileConverter,
            Converter<LoggerLevels, LoggerProfileResponse> loggerProfileConverter,
            JacksonMessageSerializationStrategy jacksonMessageSerializationStrategy) {
        this.allLoggersEndpointProber = allLoggersEndpointProber;
        this.groupLoggersEndpointProber = groupLoggersEndpointProber;
        this.oneLoggerEndpointProber = oneLoggerEndpointProber;
        this.setOneLoggerEndpointProber = setOneLoggerEndpointProber;
        this.setForLoggerGroupEndpointProber = setForLoggerGroupEndpointProber;
        this.clearForLoggerEndpointProber = clearForLoggerEndpointProber;
        this.loggersResponseConverter = loggersResponseConverter;
        this.groupProfileConverter = groupProfileConverter;
        this.loggerProfileConverter = loggerProfileConverter;
        this.jacksonMessageSerializationStrategy = jacksonMessageSerializationStrategy;
    }

    @Operation(
            summary = "Returns details of the application’s loggers.",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Loggers",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/loggers.html")
                        },
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = LoggersResponse.class))),
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
    @GetMapping(path = ApiPaths.LoggersApi.INSTANCE_ID)
    public LoggersResponse getAllLoggers(@PathVariable("instanceId") String instanceId) {
        ServiceLoggers loggers = allLoggersEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);

        return Objects.requireNonNull(loggersResponseConverter.convert(loggers));
    }

    @Operation(
            summary = "Returns details of the requested logger group.",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Loggers",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/loggers.html")
                        },
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = GroupProfileResponse.class))),
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
        @Parameter(name = "groupName", description = "The name of the logger group to find", required = true)
    })
    @GetMapping(path = ApiPaths.LoggersApi.GROUP_NAME)
    public GroupProfileResponse getGroupByName(
            @PathVariable("instanceId") String instanceId, @PathVariable("groupName") String groupName) {
        HttpPayload payload = new DefaultHttpPayload(Map.of("group.name", groupName));
        LoggerGroup group = groupLoggersEndpointProber.invoke(InstanceId.of(instanceId), payload);

        return Objects.requireNonNull(groupProfileConverter.convert(group));
    }

    @Operation(
            summary = "Returns the details of the requested logger.",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Loggers",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/loggers.html")
                        },
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = LoggerProfileResponse.class))),
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
        @Parameter(name = "loggerName", description = "The name of the logger to find", required = true)
    })
    @GetMapping(path = ApiPaths.LoggersApi.LOGGER_NAME)
    public LoggerProfileResponse getLoggerByName(
            @PathVariable("instanceId") String instanceId, @PathVariable("loggerName") String loggerName) {
        HttpPayload payload = new DefaultHttpPayload(Map.of("logger.name", loggerName));
        LoggerLevels logger = oneLoggerEndpointProber.invoke(InstanceId.of(instanceId), payload);

        return Objects.requireNonNull(loggerProfileConverter.convert(logger));
    }

    @Operation(
            summary = "The request specifies the desired logging level for a logger by its name.",
            description =
                    "Suggested logging levels that the user can select to configure the logger: OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Loggers",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/loggers.html")
                        }),
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
        @Parameter(name = "loggerName", description = "The name of the logger to find", required = true)
    })
    @PostMapping(path = ApiPaths.LoggersApi.LOGGER_NAME)
    public void setLoggingLevelByLoggerName(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("loggerName") String loggerName,
            @RequestBody LogLevelChangeRequest request) {

        HttpPayload payload = HttpPayload.json(
                Map.of("logger.name", loggerName), jacksonMessageSerializationStrategy.serialize(request));
        setOneLoggerEndpointProber.invokeNoValue(InstanceId.of(instanceId), payload);
    }

    @Operation(
            summary = "The request specifies the desired logging level for a logger group by its name.",
            description =
                    "Suggested logging levels that the user can select to configure the logger: OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Loggers",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/loggers.html")
                        }),
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
        @Parameter(name = "groupName", description = "The name of the logger group to find", required = true)
    })
    @PostMapping(path = ApiPaths.LoggersApi.GROUP_NAME)
    public void setLoggingLevelByGroupName(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("groupName") String groupName,
            @RequestBody LogLevelChangeRequest request) {

        HttpPayload payload = HttpPayload.json(
                Map.of("group.name", groupName), jacksonMessageSerializationStrategy.serialize(request));
        setForLoggerGroupEndpointProber.invokeNoValue(InstanceId.of(instanceId), payload);
    }

    @Operation(
            summary = "Clears the configured logging level of a logger, reverting it to the global/default setting.",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        links = {
                            @Link(
                                    name = "Actuator/Loggers",
                                    description = "https://docs.spring.io/spring-boot/api/rest/actuator/loggers.html")
                        }),
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
        @Parameter(name = "loggerName", description = "The name of the logger to find", required = true)
    })
    @PostMapping(path = ApiPaths.LoggersApi.CLEAR_FOR_LOGGER)
    public void clearLoggingLevelByLoggerName(
            @PathVariable("instanceId") String instanceId, @PathVariable("loggerName") String loggerName) {
        HttpPayload payload = HttpPayload.json(
                Map.of("logger.name", loggerName),
                jacksonMessageSerializationStrategy.serialize(Collections.emptyMap()));
        clearForLoggerEndpointProber.invokeNoValue(InstanceId.of(instanceId), payload);
    }
}
