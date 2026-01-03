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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * The HTTP query parameter.
 *
 * @param <T> the type of the parameter value
 * @author Mikhail Polivakha
 */
public sealed interface QueryParameter<T> permits SingleValueQueryParameter, MultiValueQueryParameter {

    /**
     * @return the key under which the parameter resides
     */
    String key();

    /**
     * @return the value of the query parameter
     */
    T value();

    /**
     * @return the encoded (URL reserved and unsafe characters are escaped) {@link String}
     * representation of {@link #value()}.
     */
    String toEncodedString();

    /**
     * Encode the given part of the URL.
     *
     * @param part part of the URL that needs to be encoded.
     * @return the encoded part of the URL.
     */
    static String encodeUrlComponent(String part) {
        return URLEncoder.encode(part, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
