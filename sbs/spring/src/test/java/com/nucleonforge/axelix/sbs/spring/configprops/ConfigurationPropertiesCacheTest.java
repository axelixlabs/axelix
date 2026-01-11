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

import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axelix.common.api.ConfigPropsFeed;
import com.nucleonforge.axelix.common.api.KeyValue;
import com.nucleonforge.axelix.sbs.spring.env.DefaultPropertyNameNormalizer;
import com.nucleonforge.axelix.sbs.spring.env.PropertyNameNormalizer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ConfigurationPropertiesCache}.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(ConfigPropsConfigurationProperties.class)
public class ConfigurationPropertiesCacheTest {

    @Autowired
    ConfigurationPropertiesCache configurationPropertiesCache;

    @Test
    void shouldReturnConfigurationProperties() {
        // when.
        ConfigPropsFeed configProps = configurationPropertiesCache.getConfigProps();

        // then.
        Set<@Nullable String> values = configProps.contexts().values().stream()
                .flatMap(it -> it.beans().values().stream())
                .flatMap(bean -> bean.properties().stream())
                .map(KeyValue::value)
                .collect(Collectors.toSet());

        // TODO: Well, the "null" sanitization policy is not something that we currently have control over.
        // It is also not clear if we want to sanitize "null" values in general. I think
        // that it makes sense to sanitize them as well, but currently it is not possible due
        // to internal implementation of the Spring Boot Actuator native config props endpoint.
        assertThat(values).containsOnly(null, "******");
        assertThat(configProps).isNotNull().isInstanceOf(ConfigPropsFeed.class);
    }

    @TestConfiguration
    static class ConfigurationPropertiesCacheTestConfiguration {

        @Bean
        public ConfigurationPropertiesConverter configurationPropertiesConverter() {
            return new FlatteningConfigurationPropertiesConverter();
        }

        @Bean
        public PropertyNameNormalizer propertyNameNormalizer() {
            return new DefaultPropertyNameNormalizer();
        }

        @Bean
        public SmartSanitizingFunction smartSanitizingFunction(PropertyNameNormalizer propertyNameNormalizer) {
            return new SmartSanitizingFunction(ConfigPropsConfigurationProperties.SANITIZE_ALL, propertyNameNormalizer);
        }

        @Bean
        public ConfigurationPropertiesCache configurationPropertiesCache(
                SmartSanitizingFunction smartSanitizingFunction,
                ApplicationContext applicationContext,
                ConfigurationPropertiesConverter configurationPropertiesConverter) {
            return new ConfigurationPropertiesCache(
                    smartSanitizingFunction, applicationContext, configurationPropertiesConverter);
        }
    }
}
