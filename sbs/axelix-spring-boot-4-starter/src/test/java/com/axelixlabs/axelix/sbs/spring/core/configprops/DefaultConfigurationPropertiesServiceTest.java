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

import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jpa.autoconfigure.JpaProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesEntry;
import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.utils.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigPropsTestSupportConfiguration.SharedAxelixConfigurationProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/**
 * Integration tests for {@link DefaultConfigurationPropertiesService}.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 * @author Artemiy Degtyarev
 */
public class DefaultConfigurationPropertiesServiceTest extends AbstractConfigPropsSharedContextTest {

    @Autowired
    private ConfigurationPropertiesService configurationPropertiesService;

    @Test
    void shouldReturnBeanValuesAndBindingInputs() {
        // when.
        ConfigurationPropertiesFeed feed = configurationPropertiesService.getConfigProps();

        // then.
        assertThat(feed.getBeans())
                .filteredOn(bean -> bean.getPrefix().equals("axelix.prop.test"))
                .singleElement()
                .satisfies(bean -> {
                    assertThat(bean.getBeanName()).isEqualTo(SharedAxelixConfigurationProperties.class.getName());
                    assertThat(bean.getProperties())
                            .extracting(
                                    ConfigurationPropertiesEntry::getKey,
                                    ConfigurationPropertiesEntry::getValue,
                                    ConfigurationPropertiesEntry::getSensitive)
                            .contains(tuple("tags.forSanitization", "toBeSanitized", true));
                    assertThat(bean.getInputs())
                            .extracting(ConfigurationPropertiesEntry::getKey, ConfigurationPropertiesEntry::getValue)
                            .contains(
                                    tuple("httpClient.requests[0].baseUrl.value", "https://api.users.example.com/v1"),
                                    tuple("enabledContexts[0].value", "user-service,payment-service"));
                    assertThat(bean.getInputs())
                            .filteredOn(entry -> entry.getKey().equals("httpClient.requests[0].baseUrl.origin"))
                            .singleElement()
                            .satisfies(entry -> assertThat(entry.getValue()).contains("Inlined Test Properties"));
                });
    }

    @Test
    void shouldIncludeUnsetJpaProperties() {
        // given.
        new ApplicationContextRunner()
                .withUserConfiguration(JpaConfiguration.class)
                .run(context -> {
                    ConfigurationPropertiesService service = new DefaultConfigurationPropertiesService(
                            context,
                            new DefaultConfigurationPropertiesFlattener(),
                            new PropertySensitivityMarker(List.of(), new DefaultPropertyNameNormalizer()));

                    // when.
                    ConfigurationPropertiesFeed feed = service.getConfigProps();

                    // then.
                    assertThat(feed.getBeans())
                            .filteredOn(bean -> bean.getPrefix().equals("spring.jpa"))
                            .singleElement()
                            .satisfies(bean -> assertThat(bean.getProperties())
                                    .extracting(
                                            ConfigurationPropertiesEntry::getKey,
                                            ConfigurationPropertiesEntry::getValue)
                                    .contains(
                                            tuple("databasePlatform", null),
                                            tuple("database", null),
                                            tuple("openInView", null),
                                            tuple("showSql", "false")));
                });
    }

    @Test
    void shouldKeepBoundEmptyCollectionWithoutInputValue() {
        // given.
        new ApplicationContextRunner()
                .withUserConfiguration(JpaConfiguration.class)
                .withPropertyValues("empty.values=")
                .run(context -> {
                    ConfigurationPropertiesService service = new DefaultConfigurationPropertiesService(
                            context,
                            new DefaultConfigurationPropertiesFlattener(),
                            new PropertySensitivityMarker(List.of(), new DefaultPropertyNameNormalizer()));

                    // when.
                    ConfigurationPropertiesFeed feed = service.getConfigProps();

                    // then.
                    assertThat(feed.getBeans())
                            .filteredOn(bean -> bean.getPrefix().equals("empty"))
                            .singleElement()
                            .satisfies(bean -> {
                                assertThat(bean.getProperties())
                                        .extracting(
                                                ConfigurationPropertiesEntry::getKey,
                                                ConfigurationPropertiesEntry::getValue)
                                        .containsExactly(tuple("values", null));
                                assertThat(bean.getInputs())
                                        .extracting(
                                                ConfigurationPropertiesEntry::getKey,
                                                ConfigurationPropertiesEntry::getValue)
                                        .containsExactly(tuple("values", null));
                            });
                });
    }

    @ConfigurationProperties("empty")
    record EmptyCollectionProperties(List<String> values) {}

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({JpaProperties.class, EmptyCollectionProperties.class})
    static class JpaConfiguration {}
}
