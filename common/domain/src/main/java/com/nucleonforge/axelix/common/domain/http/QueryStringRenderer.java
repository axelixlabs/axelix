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
 * Utility class for working with HTTP query parameters.
 *
 * @author Mikhail Polivakha
 */
public class QueryStringRenderer {

    /**
     * Renders the array of {@link QueryParameter query paramters} to the query parameter string.
     *
     * @return the rendered query parameters string, e.g. {@code ?key=v1&key2=v2}
     */
    public static String renderQueryString(List<QueryParameter<?>> queryParameters) {
        if (queryParameters.isEmpty()) {
            return "";
        }

        return queryParameters.stream().map(QueryParameter::toEncodedString).collect(Collectors.joining("&", "?", ""));
    }
}
