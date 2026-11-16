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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.support.GenericApplicationContext;

import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesEntry;
import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.utils.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigPropsTestSupportConfiguration.SharedAxelixConfigurationProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Integration tests for {@link DefaultConfigurationPropertiesService}.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 * @author Artemiy Degtyarev
 */
public class DefaultConfigurationPropertiesServiceTest extends AbstractConfigPropsSharedContextTest {

    @Autowired
    private ConfigurationPropertiesService configurationPropertiesService;

    @Test
    void includesParentContextPropertiesAndUnsetValues() {
        // given.
        try (GenericApplicationContext parent = new GenericApplicationContext()) {
            parent.registerBean("parent", NullableProperties.class);
            parent.refresh();
            new ApplicationContextRunner().withParent(parent).run(context -> {
                ConfigurationPropertiesService service = new DefaultConfigurationPropertiesService(
                        context,
                        new DefaultConfigurationPropertiesFlattener(),
                        new PropertySensitivityMarker(List.of(), new DefaultPropertyNameNormalizer()));

                // when.
                ConfigurationPropertiesFeed feed = service.getConfigProps();

                // then.
                assertThat(feed.getBeans())
                        .filteredOn(bean -> bean.getBeanName().equals("parent"))
                        .singleElement()
                        .satisfies(bean -> {
                            assertThat(bean.getPrefix()).isEqualTo("nullable");
                            assertThat(bean.getProperties())
                                    .extracting(
                                            ConfigurationPropertiesEntry::getKey,
                                            ConfigurationPropertiesEntry::getValue)
                                    .contains(tuple("unset", null));
                        });
            });
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void releasesProxyTargetEvenWhenSerializationFails(boolean fails) throws Exception {
        // given.
        NullableProperties target = new NullableProperties();
        TargetSource source = mock(TargetSource.class);
        org.mockito.Mockito.doReturn(NullableProperties.class).when(source).getTargetClass();
        when(source.getTarget()).thenReturn(target);
        ProxyFactory factory = new ProxyFactory();
        factory.setTargetSource(source);
        factory.setProxyTargetClass(true);
        if (fails) {
            target.getValues().put("broken", new Object() {
                @com.fasterxml.jackson.annotation.JsonValue
                public String value() {
                    throw new IllegalStateException();
                }
            });
        }
        NullableProperties proxy = (NullableProperties) factory.getProxy();
        new ApplicationContextRunner()
                .withBean("proxied", NullableProperties.class, () -> proxy)
                .run(context -> {
                    ConfigurationPropertiesService service = new DefaultConfigurationPropertiesService(
                            context,
                            new DefaultConfigurationPropertiesFlattener(),
                            new PropertySensitivityMarker(List.of(), new DefaultPropertyNameNormalizer()));

                    // when.
                    ConfigurationPropertiesFeed feed = service.getConfigProps();

                    // then.
                    assertThat(feed.getBeans())
                            .filteredOn(bean -> bean.getBeanName().equals("proxied"))
                            .singleElement()
                            .satisfies(bean -> assertThat(bean.getProperties())
                                    .extracting(ConfigurationPropertiesEntry::getKey)
                                    .contains(fails ? "error" : "unset"));
                    verify(source).releaseTarget(target);
                });
    }

    @ConfigurationProperties("nullable")
    public static class NullableProperties {
        public @Nullable String getUnset() {
            return null;
        }

        public void setUnset(@Nullable String unset) {}

        private Map<String, Object> values = new java.util.HashMap<>();

        public Map<String, Object> getValues() {
            return values;
        }

        public void setValues(Map<String, Object> values) {
            this.values = values;
        }
    }

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
        assertThat(configProps.getBeans())
                .flatExtracting(bean -> bean.getProperties())
                .allSatisfy(property -> assertThat(property.getSensitive()).isTrue());
    }

    @Test
    void shouldReturnConfigurationPropertiesFeed() {
        // when.
        ConfigurationPropertiesFeed axelixConfPropDescriptor = configurationPropertiesService.getConfigProps();

        // then.

        assertThat(axelixConfPropDescriptor.getBeans()).isNotEmpty();

        assertThat(axelixConfPropDescriptor.getBeans())
                .filteredOn(beans -> beans.getPrefix().equals("axelix.prop.test"))
                .singleElement()
                .satisfies(bean -> {

                    // Bean Name
                    assertThat(bean.getBeanName()).isEqualTo(SharedAxelixConfigurationProperties.class.getName());

                    // prefix
                    assertThat(bean.getPrefix()).isEqualTo("axelix.prop.test");

                    // properties
                    assertThat(bean.getProperties())
                            .containsOnly(
                                    new ConfigurationPropertiesEntry()
                                            .sensitive(true)
                                            .key("httpClient.requests[0].methods[1].retries"),
                                    new ConfigurationPropertiesEntry()
                                            .sensitive(true)
                                            .key("tags.environment")
                                            .value("test"),
                                    new ConfigurationPropertiesEntry()
                                            .sensitive(true)
                                            .key("tags.version")
                                            .value("1.0.0"),
                                    new ConfigurationPropertiesEntry()
                                            .sensitive(true)
                                            .key("tags.forSanitization")
                                            .value("toBeSanitized"),
                                    new ConfigurationPropertiesEntry()
                                            .sensitive(true)
                                            .key("tags.FOR_SANITIZATION")
                                            .value("toBeSanitized"),
                                    new ConfigurationPropertiesEntry()
                                            .sensitive(true)
                                            .key("enabledContexts[0]")
                                            .value("user-service"),
                                    new ConfigurationPropertiesEntry()
                                            .sensitive(true)
                                            .key("enabledContexts[1]")
                                            .value("payment-service"),
                                    new ConfigurationPropertiesEntry()
                                            .sensitive(true)
                                            .key("httpClient.requests[0].name")
                                            .value("user-api"),
                                    new ConfigurationPropertiesEntry()
                                            .sensitive(true)
                                            .key("httpClient.requests[0].baseUrl")
                                            .value("https://api.users.example.com/v1"),
                                    new ConfigurationPropertiesEntry()
                                            .sensitive(true)
                                            .key("httpClient.requests[0].methods[0].type")
                                            .value("GET"),
                                    new ConfigurationPropertiesEntry()
                                            .sensitive(true)
                                            .key("httpClient.requests[0].methods[0].retries[0].count")
                                            .value("3"),
                                    new ConfigurationPropertiesEntry()
                                            .sensitive(true)
                                            .key("httpClient.requests[0].methods[0].retries[0].parameters.timeout")
                                            .value("5000"),
                                    new ConfigurationPropertiesEntry()
                                            .sensitive(true)
                                            .key("httpClient.requests[0].methods[1].type")
                                            .value("POST"),
                                    new ConfigurationPropertiesEntry()
                                            .sensitive(true)
                                            .key("httpClient.requests[1].name")
                                            .value("payment-api"),
                                    new ConfigurationPropertiesEntry()
                                            .sensitive(true)
                                            .key("httpClient.requests[1].baseUrl")
                                            .value("https://api.payments.example.com/v2"),
                                    new ConfigurationPropertiesEntry()
                                            .sensitive(true)
                                            .key("httpClient.requests[1].methods[0].type")
                                            .value("PUT"),
                                    new ConfigurationPropertiesEntry()
                                            .sensitive(true)
                                            .key("httpClient.requests[1].methods[0].retries[0].count")
                                            .value("2"),
                                    new ConfigurationPropertiesEntry()
                                            .sensitive(true)
                                            .key("httpClient.requests[1].methods[0].retries[0].parameters.log-level")
                                            .value("DEBUG"));
                });
    }
}
