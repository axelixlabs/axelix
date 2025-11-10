package com.nucleonforge.axile.master.api;

import java.util.Objects;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.api.ConditionsFeed;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.response.ConditionsFeedResponse;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.convert.Converter;
import com.nucleonforge.axile.master.service.transport.ConditionsEndpointProber;

/**
 * The API for managing conditions.
 *
 * @since 16.10.2025
 * @author Nikita Kirillov
 */
@RestController
@RequestMapping(path = ApiPaths.ConditionsApi.MAIN)
public class ConditionsApi {

    private final ConditionsEndpointProber conditionsEndpointProber;
    private final Converter<ConditionsFeed, ConditionsFeedResponse> converter;

    public ConditionsApi(
            ConditionsEndpointProber conditionsEndpointProber,
            Converter<ConditionsFeed, ConditionsFeedResponse> converter) {
        this.conditionsEndpointProber = conditionsEndpointProber;
        this.converter = converter;
    }

    @GetMapping(path = ApiPaths.ConditionsApi.FEED)
    public ConditionsFeedResponse getConditionsFeed(@PathVariable("instanceId") String instanceId) {
        ConditionsFeed result = conditionsEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
        return Objects.requireNonNull(converter.convert(result));
    }
}
