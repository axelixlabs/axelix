package com.nucleonforge.axile.master.service.convert.configprops;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.common.api.ConfigpropsFeed;
import com.nucleonforge.axile.master.api.response.KeyValue;
import com.nucleonforge.axile.master.api.response.configprops.ConfigpropsProfile;
import com.nucleonforge.axile.master.service.convert.Converter;

/**
 * Abstract {@link Converter} for the Configprops endpoint,
 * converting a {@link ConfigpropsFeed} into a target response type.
 *
 * @param <R> the target response type
 *
 * @since 02.10.2025
 * @author Nikita Kirillov
 */
public abstract class AbstractConfigpropsConverter<R> implements Converter<ConfigpropsFeed, R> {

    @Override
    public @NonNull R convertInternal(@NonNull ConfigpropsFeed source) {
        return convertBeans(source.contexts().values().stream()
                .flatMap(ctx -> ctx.beans().entrySet().stream())
                .map(entry -> {
                    String beanName = entry.getKey();
                    ConfigpropsFeed.Bean bean = entry.getValue();
                    List<KeyValue> properties = flatten("", bean.properties());
                    List<KeyValue> inputs = flatten("", bean.inputs());
                    return new ConfigpropsProfile(beanName, bean.prefix(), properties, inputs);
                })
                .toList());
    }

    protected abstract R convertBeans(List<ConfigpropsProfile> beans);

    private List<KeyValue> flatten(String key, Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return List.of();
        }
        List<KeyValue> result = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String fullKey = key.isEmpty() ? entry.getKey() : key + "." + entry.getKey();
            result.addAll(flattenEntry(fullKey, entry.getValue()));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<KeyValue> flattenEntry(String key, Object value) {
        if (value instanceof Map<?, ?> map) {
            return flattenMap(key, (Map<String, Object>) map);
        }
        if (value instanceof List<?> list) {
            return flattenList(key, list);
        }
        return List.of(new KeyValue(key, value.toString()));
    }

    private List<KeyValue> flattenMap(String key, Map<String, Object> map) {
        if (map.isEmpty()) {
            return List.of(new KeyValue(key, null));
        }
        return flatten(key, map);
    }

    private List<KeyValue> flattenList(String key, List<?> list) {
        if (list.isEmpty()) {
            return List.of(new KeyValue(key, null));
        }
        List<KeyValue> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Object element = list.get(i);
            String listKey = key + "[" + i + "]";
            result.addAll(flattenEntry(listKey, element));
        }
        return result;
    }
}
