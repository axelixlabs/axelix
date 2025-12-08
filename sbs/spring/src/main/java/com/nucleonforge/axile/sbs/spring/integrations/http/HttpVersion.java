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
package com.nucleonforge.axile.sbs.spring.integrations.http;

/**
 * Version of an HTTP protocol in use.
 *
 * @since 05.07.25
 * @author Mikhail Polivakha
 */
public enum HttpVersion {
    V1_0("HTTP 1/0"),
    V1_1("HTTP 1/1"),
    V2_0("HTTP 2/0"),
    V3_0("HTTP 3/0"),
    ;

    private final String display;

    HttpVersion(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}
