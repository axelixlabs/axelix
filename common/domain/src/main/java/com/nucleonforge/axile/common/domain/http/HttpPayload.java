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
package com.nucleonforge.axile.common.domain.http;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * The payload of the http requests, consisting of body and headers.
 *
 * @author Mikhail Polivakha
 */
public interface HttpPayload {

    /**
     * @return the ordered collection of {@link HttpHeader http headers}.
     */
    List<HttpHeader> headers();

    default boolean hasHeaders() {
        return !headers().isEmpty();
    }

    /**
     * @return the list of {@link QueryParameter query parameters}.
     */
    List<QueryParameter<?>> queryParameters();

    /**
     * @return map of path variable names to path variable values.
     */
    Map<String, String> pathVariableValues();

    /**
     * @return the serialized body of the http request.
     */
    byte[] requestBody();

    default boolean hasBody() {
        byte[] bytes = requestBody();

        return bytes.length != 0;
    }

    static HttpPayload json(byte[] requestBody) {
        HttpHeader contentType = new HttpHeader("Content-Type", "application/json");
        return new DefaultHttpPayload(
                List.of(contentType), Collections.emptyList(), Collections.emptyMap(), requestBody);
    }

    static HttpPayload json(Map<String, String> pathVariables, byte[] requestBody) {
        HttpHeader contentType = new HttpHeader("Content-Type", "application/json");
        return new DefaultHttpPayload(List.of(contentType), Collections.emptyList(), pathVariables, requestBody);
    }
}
