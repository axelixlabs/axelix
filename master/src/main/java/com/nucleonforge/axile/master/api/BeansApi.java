package com.nucleonforge.axile.master.api;

import java.util.Objects;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.api.BeansFeed;
import com.nucleonforge.axile.common.domain.InstanceId;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.response.BeansFeedResponse;
import com.nucleonforge.axile.master.service.convert.Converter;
import com.nucleonforge.axile.master.service.transport.BeansEndpointProber;

/**
 * The API for managing beans.
 *
 * @author Mikhail Polivakha
 */
@RestController
@RequestMapping(path = ApiPaths.BeansApi.MAIN)
public class BeansApi {

    private final BeansEndpointProber beansEndpointProber;
    private final Converter<BeansFeed, BeansFeedResponse> converter;

    public BeansApi(BeansEndpointProber beansEndpointProber, Converter<BeansFeed, BeansFeedResponse> converter) {
        this.beansEndpointProber = beansEndpointProber;
        this.converter = converter;
    }

    @RequestMapping(path = ApiPaths.BeansApi.FEED)
    public BeansFeedResponse getBeansProfile(@PathVariable("instanceId") String instanceId) {
        BeansFeed result = beansEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
        return Objects.requireNonNull(converter.convert(result));
    }
}
