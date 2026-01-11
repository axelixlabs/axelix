/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.spring.configprops;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesBeanDescriptor;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesDescriptor;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ContextConfigurationPropertiesDescriptor;

import com.nucleonforge.axelix.common.api.ConfigPropsFeed;
import com.nucleonforge.axelix.common.api.KeyValue;
import com.nucleonforge.axelix.common.utils.BeanNameUtils;

/**
 * {@link ConfigurationPropertiesConverter} that flattens out the configuration properties feed as it is returned from the
 * Spring Boot native actuator endpoint.
 *
 * @author Sergey Cherkasov
 */
public class FlatteningConfigurationPropertiesConverter implements ConfigurationPropertiesConverter {

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
