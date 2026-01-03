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
package com.nucleonforge.axelix.common.domain.http;

import java.util.List;
import java.util.stream.Collectors;

/**
 * The {@link QueryParameter} that can have multiple values. Renders to a
 * comma separated values string, e.g. {@code key=value1,value2,value3}. The correct
 * way to pass multivalued params in HTTP is not defined in any kind of RFC standard,
 * so we're just choosing this option.
 *
 * @author Mikhail Polivakha
 */
public record MultiValueQueryParameter(String key, List<String> values) implements QueryParameter<List<String>> {

    @Override
    public String key() {
        return key;
    }

    @Override
    public List<String> value() {
        return values;
    }

    @Override
    public String toEncodedString() {
        String encodedKey = QueryParameter.encodeUrlComponent(key());

        String encodedValue =
                value().stream().map(QueryParameter::encodeUrlComponent).collect(Collectors.joining(","));

        return encodedKey + "=" + encodedValue;
    }
}
