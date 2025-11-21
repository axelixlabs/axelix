package com.nucleonforge.axile.sbs.spring.configprops;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesBeanDescriptor;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesDescriptor;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ContextConfigurationPropertiesDescriptor;

import com.nucleonforge.axile.common.api.ConfigPropsFeed;
import com.nucleonforge.axile.common.api.KeyValue;
import com.nucleonforge.axile.common.utils.BeanNameUtils;

/**
 * Default implementation {@link ConfigurationPropertiesConverter}
 *
 * @author Sergey Cherkasov
 */
public class DefaultConfigurationPropertiesConverter implements ConfigurationPropertiesConverter {

    @Override
    public ConfigPropsFeed convert(ConfigurationPropertiesDescriptor originalDescriptor) {
        Map<String, ConfigPropsFeed.Context> context = originalDescriptor.getContexts().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> convertContext(e.getValue())));

        return new ConfigPropsFeed(context);
    }

    private ConfigPropsFeed.Context convertContext(ContextConfigurationPropertiesDescriptor source) {

        Map<String, ConfigPropsFeed.Bean> beans = source.getBeans().entrySet().stream()
                .collect(Collectors.toMap(
                        b -> BeanNameUtils.stripConfigPropsPrefix(b.getKey()), e -> convertBean(e.getValue())));

        return new ConfigPropsFeed.Context(beans, source.getParentId());
    }

    private ConfigPropsFeed.Bean convertBean(ConfigurationPropertiesBeanDescriptor src) {
        return new ConfigPropsFeed.Bean(
                src.getPrefix(), flatten("", src.getProperties()), flatten("", src.getInputs()));
    }

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
        return map.isEmpty() ? List.of(new KeyValue(key, null)) : flatten(key, map);
    }

    private List<KeyValue> flattenList(String key, List<?> list) {
        if (list.isEmpty()) {
            return List.of(new KeyValue(key, null));
        }

        List<KeyValue> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            String listKey = key + "[" + i + "]";
            result.addAll(flattenEntry(listKey, list.get(i)));
        }
        return result;
    }
}
