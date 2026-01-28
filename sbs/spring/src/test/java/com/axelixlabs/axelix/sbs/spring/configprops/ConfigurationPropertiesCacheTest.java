/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.configprops;

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

import com.axelixlabs.axelix.common.api.ConfigPropsFeed;
import com.axelixlabs.axelix.common.api.KeyValue;
import com.axelixlabs.axelix.sbs.spring.config.EndpointsConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.env.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.env.PropertyNameNormalizer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ConfigurationPropertiesCache}.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(EndpointsConfigurationProperties.class)
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
            return new SmartSanitizingFunction(EndpointsConfigurationProperties.SANITIZE_ALL, propertyNameNormalizer);
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
