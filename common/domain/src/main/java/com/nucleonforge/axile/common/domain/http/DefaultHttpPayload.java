/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.common.domain.http;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link HttpPayload}.
 *
 * @since 02.09.2025
 * @author Nikita Kirillov
 */
public record DefaultHttpPayload(
        List<HttpHeader> headers,
        List<QueryParameter<?>> queryParameters,
        Map<String, String> pathVariableValues,
        byte[] requestBody)
        implements HttpPayload {

    public DefaultHttpPayload(List<HttpHeader> headers) {
        this(headers, Collections.emptyList(), Collections.emptyMap(), new byte[0]);
    }

    public DefaultHttpPayload(Map<String, String> pathVariableValues) {
        this(Collections.emptyList(), Collections.emptyList(), pathVariableValues, new byte[0]);
    }

    public DefaultHttpPayload(Map<String, String> pathVariableValues, byte[] requestBody) {
        this(Collections.emptyList(), Collections.emptyList(), pathVariableValues, requestBody);
    }

    public DefaultHttpPayload(List<HttpHeader> headers, byte[] requestBody) {
        this(headers, Collections.emptyList(), Collections.emptyMap(), requestBody);
    }

    public DefaultHttpPayload(List<QueryParameter<?>> queryParameters, Map<String, String> pathVariableValues) {
        this(Collections.emptyList(), queryParameters, pathVariableValues, new byte[0]);
    }
}
