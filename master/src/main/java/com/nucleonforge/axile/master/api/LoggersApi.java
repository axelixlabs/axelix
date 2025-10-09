package com.nucleonforge.axile.master.api;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.api.loggers.LoggerGroup;
import com.nucleonforge.axile.common.api.loggers.LoggerLevels;
import com.nucleonforge.axile.common.api.loggers.ServiceLoggers;
import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.request.LogLevelChangeRequest;
import com.nucleonforge.axile.master.api.response.loggers.GroupProfileResponse;
import com.nucleonforge.axile.master.api.response.loggers.LoggerProfileResponse;
import com.nucleonforge.axile.master.api.response.loggers.LoggersResponse;
import com.nucleonforge.axile.master.model.instance.InstanceId;
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

    @GetMapping(path = ApiPaths.LoggersApi.INSTANCE_ID)
    public LoggersResponse getAllLoggers(@PathVariable("instanceId") String instanceId) {
        ServiceLoggers loggers = allLoggersEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);

        return Objects.requireNonNull(loggersResponseConverter.convert(loggers));
    }

    @GetMapping(path = ApiPaths.LoggersApi.GROUP_NAME)
    public GroupProfileResponse getGroupByName(
            @PathVariable("instanceId") String instanceId, @PathVariable("groupName") String groupName) {
        HttpPayload payload = new DefaultHttpPayload(Map.of("group.name", groupName));
        LoggerGroup group = groupLoggersEndpointProber.invoke(InstanceId.of(instanceId), payload);

        return Objects.requireNonNull(groupProfileConverter.convert(group));
    }

    @GetMapping(path = ApiPaths.LoggersApi.LOGGER_NAME)
    public LoggerProfileResponse getLoggerByName(
            @PathVariable("instanceId") String instanceId, @PathVariable("loggerName") String loggerName) {
        HttpPayload payload = new DefaultHttpPayload(Map.of("logger.name", loggerName));
        LoggerLevels logger = oneLoggerEndpointProber.invoke(InstanceId.of(instanceId), payload);

        return Objects.requireNonNull(loggerProfileConverter.convert(logger));
    }

    @PostMapping(path = ApiPaths.LoggersApi.LOGGER_NAME)
    public void setLoggingLevelByLoggerName(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("loggerName") String loggerName,
            @RequestBody LogLevelChangeRequest request) {

        HttpPayload payload = HttpPayload.json(
                Map.of("logger.name", loggerName), jacksonMessageSerializationStrategy.serialize(request));
        setOneLoggerEndpointProber.invokeNoValue(InstanceId.of(instanceId), payload);
    }

    @PostMapping(path = ApiPaths.LoggersApi.GROUP_NAME)
    public void setLoggingLevelByGroupName(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("groupName") String groupName,
            @RequestBody LogLevelChangeRequest request) {

        HttpPayload payload = HttpPayload.json(
                Map.of("group.name", groupName), jacksonMessageSerializationStrategy.serialize(request));
        setForLoggerGroupEndpointProber.invokeNoValue(InstanceId.of(instanceId), payload);
    }

    @PostMapping(path = ApiPaths.LoggersApi.CLEAR_FOR_LOGGER)
    public void clearLoggingLevelByLoggerName(
            @PathVariable("instanceId") String instanceId, @PathVariable("loggerName") String loggerName) {
        HttpPayload payload = HttpPayload.json(
                Map.of("logger.name", loggerName),
                jacksonMessageSerializationStrategy.serialize(Collections.emptyMap()));
        clearForLoggerEndpointProber.invokeNoValue(InstanceId.of(instanceId), payload);
    }
}
