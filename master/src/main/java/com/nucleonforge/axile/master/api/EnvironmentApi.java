package com.nucleonforge.axile.master.api;

import java.util.Map;
import java.util.Objects;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.api.env.EnvironmentFeed;
import com.nucleonforge.axile.common.api.env.EnvironmentProperty;
import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.response.EnvironmentFeedResponse;
import com.nucleonforge.axile.master.api.response.EnvironmentPropertyResponse;
import com.nucleonforge.axile.master.service.convert.Converter;
import com.nucleonforge.axile.master.service.transport.EnvironmentEndpointProber;
import com.nucleonforge.axile.master.service.transport.EnvironmentPropertyEndpointProber;

/**
 * The API for managing environment.
 *
 * @since 27.08.2025
 * @author Nikita Kirillov
 */
@RestController
@RequestMapping(path = ApiPaths.EnvironmentApi.MAIN)
public class EnvironmentApi {

    private final EnvironmentEndpointProber environmentEndpointProber;
    private final EnvironmentPropertyEndpointProber environmentPropertyEndpointProber;
    private final Converter<EnvironmentFeed, EnvironmentFeedResponse> envConverter;
    private final Converter<EnvironmentProperty, EnvironmentPropertyResponse> envPropertyConverter;

    public EnvironmentApi(
            EnvironmentEndpointProber environmentEndpointProber,
            EnvironmentPropertyEndpointProber environmentPropertyEndpointProber,
            Converter<EnvironmentFeed, EnvironmentFeedResponse> envConverter,
            Converter<EnvironmentProperty, EnvironmentPropertyResponse> envPropertyConverter) {
        this.environmentEndpointProber = environmentEndpointProber;
        this.environmentPropertyEndpointProber = environmentPropertyEndpointProber;
        this.envConverter = envConverter;
        this.envPropertyConverter = envPropertyConverter;
    }

    @RequestMapping(path = ApiPaths.EnvironmentApi.FEED)
    public EnvironmentFeedResponse getEnvironment(@PathVariable("instanceId") String instanceId) {
        EnvironmentFeed result = environmentEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
        return Objects.requireNonNull(envConverter.convert(result));
    }

    @RequestMapping(path = ApiPaths.EnvironmentApi.PROPERTY)
    public EnvironmentPropertyResponse getProperty(
            @PathVariable("instanceId") String instanceId, @PathVariable("propertyName") String propertyName) {
        HttpPayload payload = new DefaultHttpPayload(Map.of("property.name", propertyName));

        EnvironmentProperty result = environmentPropertyEndpointProber.invoke(InstanceId.of(instanceId), payload);
        return Objects.requireNonNull(envPropertyConverter.convert(result));
    }
}
