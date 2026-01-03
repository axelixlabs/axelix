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

/**
 * The most regular type of the parameter - single parameter http query
 * parameter, for instance {@code ?first=value} or {@code ?second=123}.
 *
 * @author Mikhail Polivakha
 */
public record SingleValueQueryParameter(String key, String value) implements QueryParameter<String> {

    @Override
    public String key() {
        return key;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public String toEncodedString() {
        String encodedKey = QueryParameter.encodeUrlComponent(key());

        String encodedValue = QueryParameter.encodeUrlComponent(value());

        return encodedKey + "=" + encodedValue;
    }
}
