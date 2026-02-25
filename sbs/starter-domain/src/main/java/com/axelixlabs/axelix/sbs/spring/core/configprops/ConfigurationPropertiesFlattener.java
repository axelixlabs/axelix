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

import java.util.List;
import java.util.Map;

import com.axelixlabs.axelix.common.api.KeyValue;

/**
 * Flattens nested configuration properties into a linear list of key-value pairs.
 * <p>
 * Implementations typically convert nested maps/lists into a dot/bracket key notation
 * (for example, {@code app.datasource.url} or {@code app.servers[0].host}) so the
 * resulting values can be transferred or displayed without preserving the original object graph.
 *
 * @author Sergey Cherkasov
 */
public interface ConfigurationPropertiesFlattener {

    /**
     * Flattens the provided configuration map without a root key prefix.
     *
     * @param map source configuration properties map to flatten.
     *
     * @return flattened key-value pairs, or an empty list when there is nothing to flatten.
     */
    default List<KeyValue> flatten(Map<String, Object> map) {
        return flatten("", map);
    }

    /**
     * Flattens the provided configuration map using the given key prefix.
     * <p>
     * The {@code key} parameter is used as the root path and can be empty. Returned items contain
     * terminal values only; nested structures are recursively expanded by implementation-specific rules.
     *
     * @param key root key prefix for flattened entries; may be empty.
     * @param map source configuration properties map to flatten.
     *
     * @return flattened key-value pairs, or an empty list when there is nothing to flatten.
     */
    List<KeyValue> flatten(String key, Map<String, Object> map);
}
