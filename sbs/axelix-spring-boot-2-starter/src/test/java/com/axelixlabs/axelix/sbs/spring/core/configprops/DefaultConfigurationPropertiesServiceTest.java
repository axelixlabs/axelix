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
package com.axelixlabs.axelix.sbs.spring.core.configprops;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesEntry;
import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesFeed;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultConfigurationPropertiesService}.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
@SpringBootTest(
        properties = {
            "axelix.prop.test.tags.forSanitization=toBeSanitized",
            "axelix.prop.test.tags.FOR_SANITIZATION=toBeSanitized"
        })
@EnableConfigurationProperties(DefaultConfigurationPropertiesServiceTest.AxelixConfigurationProperties.class)
@Import(DefaultConfigurationPropertiesServiceTest.ConfigurationPropertiesServiceTestConfiguration.class)
public class DefaultConfigurationPropertiesServiceTest {

    @Autowired
    private ConfigurationPropertiesService configurationPropertiesService;

    @Test
    void shouldReturnConfigurationPropertiesWithoutSanitization() {
        // when.
        ConfigurationPropertiesFeed configProps = configurationPropertiesService.getConfigProps();

        // then.
        Set<@Nullable String> values = configProps.getBeans().stream()
                .flatMap(bean -> bean.getProperties().stream())
                .map(ConfigurationPropertiesEntry::getValue)
                .collect(Collectors.toSet());

        assertThat(values).contains("toBeSanitized").doesNotContain("******");
    }

    @ConfigurationProperties(prefix = "axelix.prop.test")
    static class AxelixConfigurationProperties {

        private Map<String, String> tags;

        public Map<String, String> getTags() {
            return tags;
        }

        public void setTags(Map<String, String> tags) {
            this.tags = tags;
        }
    }

    static class ConfigurationPropertiesServiceTestConfiguration {

        @Bean
        public ConfigurationPropertiesFlattener configurationPropertiesFlattener() {
            return new DefaultConfigurationPropertiesFlattener();
        }

        @Bean
        public ConfigurationPropertiesConverter configurationPropertiesConverter(
                ConfigurationPropertiesFlattener configurationPropertiesFlattener) {
            return new DefaultConfigurationPropertiesConverter(configurationPropertiesFlattener);
        }

        @Bean
        public ConfigurationPropertiesService configurationPropertiesService(
                ApplicationContext applicationContext,
                ConfigurationPropertiesConverter configurationPropertiesConverter) {
            return new DefaultConfigurationPropertiesService(applicationContext, configurationPropertiesConverter);
        }
    }
}
