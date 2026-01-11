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
package com.nucleonforge.axelix.sbs.spring.configprops;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties that for the {@link AxelixConfigurationPropertiesEndpoint}.
 *
 * @author Mikhail Polivakha
 */
@ConfigurationProperties(prefix = "axelix.sbs.endpoints.config-props")
public class ConfigPropsConfigurationProperties {

    public static final List<String> SANITIZE_ALL = List.of("*");

    /**
     * List of properties whose values needs to be sanitized before being returned.
     * Single value of {@code "*"} means all properties must be sanitized.
     */
    private List<String> sanitizedProperties = List.of();

    public List<String> getSanitizedProperties() {
        return sanitizedProperties;
    }

    public ConfigPropsConfigurationProperties setSanitizedProperties(List<String> sanitizedProperties) {
        this.sanitizedProperties = sanitizedProperties;
        return this;
    }
}
