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

import java.util.Arrays;
import java.util.List;

/**
 * Single http header. Headers are potentially multivalued in http, and
 * therefore we have {@link List} of values here.
 *
 * @author Mikhail Polivakha
 */
public record HttpHeader(String name, List<String> values) {

    public HttpHeader(String name, String... values) {
        this(name, Arrays.stream(values).toList());
    }

    /**
     * @return produced the single {@link String} value for a given HTTP header.
     */
    public String valueAsString() {
        if (values.isEmpty()) {
            return "";
        }

        return String.join(", ", values);
    }
}
