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
package com.axelixlabs.axelix.sbs.spring.core.configprops;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.api.KeyValue;

/**
 * Default implementation {@link ConfigurationPropertiesFlattener}.
 *
 * @author Sergey Cherkasov
 */
public class DefaultConfigurationPropertiesFlattener implements ConfigurationPropertiesFlattener {

    @Override
    public List<KeyValue> flatten(String key, Map<String, ? extends @Nullable Object> map) {
        if (map == null || map.isEmpty()) {
            return List.of();
        }
        List<KeyValue> result = new ArrayList<>();
        for (Map.Entry<String, ? extends @Nullable Object> entry : map.entrySet()) {
            String fullKey = key.isEmpty() ? entry.getKey() : key + "." + entry.getKey();
            result.addAll(flattenEntry(fullKey, entry.getValue()));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<KeyValue> flattenEntry(String key, @Nullable Object value) {

        if (value instanceof Map) {
            Map<?, ? extends @Nullable Object> map = (Map<?, ?>) value;
            return flattenMap(key, (Map<String, @Nullable Object>) map);
        }

        if (value instanceof List) {
            List<?> list = (List<?>) value;
            return flattenList(key, list);
        }

        return List.of(new KeyValue(key, String.valueOf(value)));
    }

    private List<KeyValue> flattenMap(String key, Map<String, @Nullable Object> map) {
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
