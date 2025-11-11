package com.nucleonforge.axile.master.service.state;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.common.api.ConfigpropsFeed;
import com.nucleonforge.axile.common.domain.http.NoHttpPayload;
import com.nucleonforge.axile.master.api.response.configprops.ConfigpropsFeedResponse;
import com.nucleonforge.axile.master.exception.InstanceNotFoundException;
import com.nucleonforge.axile.master.model.instance.InstanceId;
import com.nucleonforge.axile.master.service.convert.Converter;
import com.nucleonforge.axile.master.service.transport.confogprops.ConfigpropsEndpointProber;

@Component
public class InMemoryConfigpropsFeed {

    private final ConcurrentHashMap<String, ConfigpropsFeedResponse> cache;
    private final ConfigpropsEndpointProber configpropsEndpointProber;
    private final Converter<ConfigpropsFeed, ConfigpropsFeedResponse> configpropsFeedConverter;

    public InMemoryConfigpropsFeed(
            ConfigpropsEndpointProber configpropsEndpointProber,
            Converter<ConfigpropsFeed, ConfigpropsFeedResponse> configpropsFeedConverter) {
        this.cache = new ConcurrentHashMap<>();
        this.configpropsEndpointProber = configpropsEndpointProber;
        this.configpropsFeedConverter = configpropsFeedConverter;
    }

    public void add(String instanceId) {
        ConfigpropsFeed result = configpropsEndpointProber.invoke(InstanceId.of(instanceId), NoHttpPayload.INSTANCE);
        this.cache.putIfAbsent(instanceId, configpropsFeedConverter.convert(result));
    }

    public Optional<ConfigpropsFeedResponse> get(String instanceId) {
        return Optional.ofNullable(cache.get(instanceId));
    }

    public void delete(String instanceId) throws InstanceNotFoundException {
        ConfigpropsFeedResponse oldValue = cache.remove(instanceId);

        if (oldValue == null) {
            throw new InstanceNotFoundException();
        }
    }

    public Map<String, String> getMap(String instanceId) {
        ConfigpropsFeedResponse response = cache.get(instanceId);

        return response == null
                ? Map.of()
                : response.beans().stream()
                        .flatMap(bean -> bean.properties().stream()
                                .map(prop -> Map.entry(bean.prefix() + "." + prop.key(), bean.beanName())))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
