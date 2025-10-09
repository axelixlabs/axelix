package com.nucleonforge.axile.master.api;

import java.util.Map;
import java.util.Objects;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.api.ConfigpropsFeed;
import com.nucleonforge.axile.common.domain.http.DefaultHttpPayload;
import com.nucleonforge.axile.common.domain.http.HttpPayload;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.response.configprops.ConfigpropsByPrefixResponse;
import com.nucleonforge.axile.master.api.response.configprops.ConfigpropsFeedResponse;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.convert.Converter;
import com.nucleonforge.axile.master.service.transport.confogprops.ConfigpropsByPrefixEndpointProber;
import com.nucleonforge.axile.master.service.transport.confogprops.ConfigpropsEndpointProber;

/**
 * The API for managing configprops.
 *
 * @author Sergey Cherkasov
 */
@RestController
@RequestMapping(path = ApiPaths.ConfigpropsApi.MAIN)
public class ConfigpropsApi {

    private final ConfigpropsEndpointProber configpropsEndpointProber;
    private final ConfigpropsByPrefixEndpointProber configpropsByPrefixEndpointProber;
    private final Converter<ConfigpropsFeed, ConfigpropsFeedResponse> configpropsFeedConverter;
    private final Converter<ConfigpropsFeed, ConfigpropsByPrefixResponse> configpropsByPrefixResponseConverter;

    public ConfigpropsApi(
            ConfigpropsEndpointProber configpropsEndpointProber,
            ConfigpropsByPrefixEndpointProber configpropsByPrefixEndpointProber,
            Converter<ConfigpropsFeed, ConfigpropsFeedResponse> configpropsFeedConverter,
            Converter<ConfigpropsFeed, ConfigpropsByPrefixResponse> configpropsByPrefixResponseConverter) {
        this.configpropsEndpointProber = configpropsEndpointProber;
        this.configpropsByPrefixEndpointProber = configpropsByPrefixEndpointProber;
        this.configpropsFeedConverter = configpropsFeedConverter;
        this.configpropsByPrefixResponseConverter = configpropsByPrefixResponseConverter;
    }

    @GetMapping(path = ApiPaths.ConfigpropsApi.FEED)
    public ConfigpropsFeedResponse getBeansFeedProfile(@PathVariable("instanceId") String instanceId) {
        ConfigpropsFeed result = configpropsEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
        return Objects.requireNonNull(configpropsFeedConverter.convert(result));
    }

    @GetMapping(path = ApiPaths.ConfigpropsApi.BEAN_BY_PREFIX)
    public ConfigpropsByPrefixResponse getBeanByPrefixProfile(
            @PathVariable("instanceId") String instanceId, @PathVariable("prefix") String prefix) {
        HttpPayload payload = new DefaultHttpPayload(Map.of("prefix", prefix));
        ConfigpropsFeed result = configpropsByPrefixEndpointProber.invoke(InstanceId.of(instanceId), payload);
        return Objects.requireNonNull(configpropsByPrefixResponseConverter.convert(result));
    }
}
