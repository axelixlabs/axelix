/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.configprops;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesBeanDescriptor;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesDescriptor;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ContextConfigurationPropertiesDescriptor;

import com.axelixlabs.axelix.common.api.ConfigPropsFeed;
import com.axelixlabs.axelix.common.api.KeyValue;
import com.axelixlabs.axelix.common.utils.BeanNameUtils;

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
