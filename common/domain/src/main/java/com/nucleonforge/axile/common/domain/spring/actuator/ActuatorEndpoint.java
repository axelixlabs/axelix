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
package com.nucleonforge.axile.common.domain.spring.actuator;

import com.nucleonforge.axile.common.domain.http.HttpMethod;
import com.nucleonforge.axile.common.domain.http.HttpUrl;

/**
 * Spring Actuator Endpoint.
 *
 * @param httpMethod the HTTP method by which this actuator endpoint should be reached.
 * @param path the specific path for this actuator endpoint, that follows the {@code /actuator}. For instance, for the
 *      beans endpoint, the path would be {@literal /beans}
 * @author Mikhail Polivakha
 */
public record ActuatorEndpoint(HttpUrl path, HttpMethod httpMethod) {

    public static ActuatorEndpoint of(String path, HttpMethod httpMethod) {
        HttpUrl httpUrl = new HttpUrl(path);
        return new ActuatorEndpoint(httpUrl, httpMethod);
    }
}
