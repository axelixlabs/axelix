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

import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesDescriptor;

import com.nucleonforge.axelix.common.api.ConfigPropsFeed;

/**
 * Interface that is capable to convert values from type {@code ConfigurationPropertiesDescriptor}
 * to type {@link ConfigPropsFeed}.
 *
 * @author Sergey Cherkasov
 */
public interface ConfigurationPropertiesConverter {

    /**
     * Converts the original configprops response of type {@code ConfigurationPropertiesDescriptor}
     * to type {@link ConfigPropsFeed}.
     *
     * @param originalDescriptor the original {@code @ConfigurationProperties} descriptor from Spring Boot
     * @return converted {@code @ConfigurationProperties} descriptor
     */
    ConfigPropsFeed convert(ConfigurationPropertiesDescriptor originalDescriptor);
}
