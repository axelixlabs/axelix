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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The abstraction over the HTTP URL string.
 *
 * @author Mikhail Polivakha
 */
public final class HttpUrl {

    private static final String PATH_VARIABLE_OPEN = "{";
    private static final String PATH_VARIABLE_CLOSE = "}";

    private final List<PathVariable> pathVariables;
    private final String originalUrl;

    /**
     * Example of usage:
     * <pre class="code">
     * Example:
     * Input: /cache/{cache}/{some.other.value}
     * Output: ["cache", "some.other.value"]
     * <pre/>
     *
     * @param url <strong>templated</strong> url to be parsed, see in the example
     */
    public HttpUrl(String url) {
        this.pathVariables = parseUrlTemplate(url);
        this.originalUrl = url;
    }

    /**
     * Expand the given url, meaning, replace all variables in the path with given values
     * and add {@link QueryParameter query parameters}.
     */
    public String expand(Map<String, String> pathVariableValues, List<QueryParameter<?>> queryParameters) {
        StringBuilder result = new StringBuilder(originalUrl);

        List<PathVariable> reversed = new ArrayList<>(pathVariables);
        Collections.reverse(reversed);

        for (int i = 0; i < reversed.size(); i++) {
            PathVariable pathVariable = reversed.get(i);
            String variableValue = pathVariableValues.get(pathVariable.name());
            if (variableValue != null) {
                result.replace(pathVariable.starts(), pathVariable.ends(), variableValue);
            } else if (i == 0) {
                // omitting the last path variable if not present. -1 to account for leading slash ('/')
                result.replace(pathVariable.starts() - 1, pathVariable.ends(), "");
            }
        }

        String queryString = QueryStringRenderer.renderQueryString(queryParameters);
        return result.append(queryString).toString();
    }

    /**
     * Variable in the URL path. Defined by name and its position in the URL.
     * <p>
     * Pattern for a given variable in URL is <pre>{ + variable name + }</pre>.
     *
     * @param starts the index since the path variable starts in the request, inclusive.
     * @param ends the index until which path variable spans, exclusive.
     * @param name the name of the path variable.
     */
    record PathVariable(String name, int starts, int ends) {}

    /**
     * Parses the <strong>templated</strong> URL in order to extract the path variables used in the request.
     *
     * @param url <strong>templated</strong> url to be parsed, see in the example.
     * @return the list of path variables that have been used in this URL.
     */
    private static List<PathVariable> parseUrlTemplate(String url) {

        if (url.isEmpty()) {
            return List.of();
        }

        int left, right = 0;
        List<PathVariable> pathVariables = new ArrayList<>();

        while ((left = url.indexOf(PATH_VARIABLE_OPEN, right)) != -1) {
            right = url.indexOf(PATH_VARIABLE_CLOSE, left);

            // no closing bracket found - assuming no pattern found
            if (right == -1) {
                break;
            }

            pathVariables.add(new PathVariable(
                    url.substring(left + PATH_VARIABLE_OPEN.length(), right),
                    left,
                    right + PATH_VARIABLE_CLOSE.length()));
        }

        return pathVariables;
    }
}
