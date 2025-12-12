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
package com.nucleonforge.axile.master.autoconfiguration.security;

import java.util.ArrayList;
import java.util.List;

/**
 * CORS configuration properties.
 *
 * @since 11.12.2025
 * @author Nikita Kirillov
 */
@SuppressWarnings("NullAway.Init")
public class CorsProperties {

    /**
     * List of allowed origins.
     */
    private List<String> allowedOrigins;

    /**
     * List of allowed HTTP methods.
     * <p><b>Example:</b> GET, POST, PUT, DELETE, OPTIONS, PATCH</p>
     */
    private List<String> allowedMethods;

    /**
     * List of allowed headers.
     * <p><b>Example:</b> "*" (all headers)</p>
     */
    private List<String> allowedHeaders;

    /**
     * List of exposed headers.
     * <p>Headers that the browser can access in the response.</p>
     */
    private List<String> exposedHeaders = new ArrayList<>();

    /**
     * Whether to allow credentials (cookies, authorization headers).
     * <p><b>REQUIRED:</b> Must be true for cookie-based authentication.</p>
     * <p><b>Default:</b> true</p>
     */
    private boolean allowCredentials = true;

    /**
     * How long (in seconds) the browser should cache CORS preflight results.
     * <p><b>Default:</b> 3600 seconds (1 hour)</p>
     */
    private Long maxAge = 3600L;

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }

    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public Long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(Long maxAge) {
        this.maxAge = maxAge;
    }
}
