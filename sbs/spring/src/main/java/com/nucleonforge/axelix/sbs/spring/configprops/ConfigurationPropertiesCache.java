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

import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.endpoint.Show;
import org.springframework.context.ApplicationContext;

import com.nucleonforge.axelix.common.api.ConfigPropsFeed;

/**
 * Service caching the application's {@code @ConfigurationProperties}
 * data from the standard Spring Boot Actuator endpoint.
 *
 * @since 13.11.2025
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
public class ConfigurationPropertiesCache {

    private final ConfigurationPropertiesReportEndpoint delegate;
    private final ConfigurationPropertiesConverter configurationPropertiesConverter;

    @Nullable
    private volatile ConfigPropsFeed cachedResult;

    public ConfigurationPropertiesCache(
            SmartSanitizingFunction smartSanitizingFunction,
            ApplicationContext applicationContext,
            ConfigurationPropertiesConverter configurationPropertiesConverter) {
        // ALWAYS is required here in order for Spring Boot to invoke our custom sanitization function
        this.delegate = new ConfigurationPropertiesReportEndpoint(List.of(smartSanitizingFunction), Show.ALWAYS);
        this.delegate.setApplicationContext(applicationContext);
        this.configurationPropertiesConverter = configurationPropertiesConverter;
    }

    public ConfigPropsFeed getConfigProps() {
        if (cachedResult == null) {
            synchronized (this) {
                if (cachedResult == null) {
                    cachedResult = configurationPropertiesConverter.convert(delegate.configurationProperties());
                }
            }
        }
        return cachedResult;
    }
}
