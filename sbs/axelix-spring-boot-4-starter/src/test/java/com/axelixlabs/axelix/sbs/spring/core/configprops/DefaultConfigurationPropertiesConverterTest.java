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

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesDescriptor;

import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigPropsTestSupportConfiguration.SharedAxelixConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.contract.configprops.ConfigurationPropertiesEntry;
import com.axelixlabs.axelix.sbs.spring.core.contract.configprops.ConfigurationPropertiesFeed;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultConfigurationPropertiesConverter}.
 *
 * @author Sergey Cherkasov
 * @author Artemiy Degtyarev
 */
public class DefaultConfigurationPropertiesConverterTest extends AbstractConfigPropsSharedContextTest {

    @Autowired
    private ConfigurationPropertiesReportEndpoint endpoint;

    @Test
    void shouldReturnConfigurationPropertiesFeed() {
        ConfigurationPropertiesDescriptor defaultDescriptor = endpoint.configurationProperties();

        ConfigurationPropertiesFeed axelixConfPropDescriptor = converter.convert(defaultDescriptor);

        assertThat(axelixConfPropDescriptor).isNotNull();

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
                                            .key("tags.environment")
                                            .value("test"),
                                    new ConfigurationPropertiesEntry()
                                            .key("tags.version")
                                            .value("1.0.0"),
                                    new ConfigurationPropertiesEntry()
                                            .key("tags.forSanitization")
                                            .value("toBeSanitized"),
                                    new ConfigurationPropertiesEntry()
                                            .key("tags.FOR_SANITIZATION")
                                            .value("toBeSanitized"),
                                    new ConfigurationPropertiesEntry()
                                            .key("enabledContexts[0]")
                                            .value("user-service"),
                                    new ConfigurationPropertiesEntry()
                                            .key("enabledContexts[1]")
                                            .value("payment-service"),
                                    new ConfigurationPropertiesEntry()
                                            .key("httpClient.requests[0].name")
                                            .value("user-api"),
                                    new ConfigurationPropertiesEntry()
                                            .key("httpClient.requests[0].baseUrl")
                                            .value("https://api.users.example.com/v1"),
                                    new ConfigurationPropertiesEntry()
                                            .key("httpClient.requests[0].methods[0].type")
                                            .value("GET"),
                                    new ConfigurationPropertiesEntry()
                                            .key("httpClient.requests[0].methods[0].retries[0].count")
                                            .value("3"),
                                    new ConfigurationPropertiesEntry()
                                            .key("httpClient.requests[0].methods[0].retries[0].parameters.timeout")
                                            .value("5000"),
                                    new ConfigurationPropertiesEntry()
                                            .key("httpClient.requests[0].methods[1].type")
                                            .value("POST"),
                                    new ConfigurationPropertiesEntry()
                                            .key("httpClient.requests[1].name")
                                            .value("payment-api"),
                                    new ConfigurationPropertiesEntry()
                                            .key("httpClient.requests[1].baseUrl")
                                            .value("https://api.payments.example.com/v2"),
                                    new ConfigurationPropertiesEntry()
                                            .key("httpClient.requests[1].methods[0].type")
                                            .value("PUT"),
                                    new ConfigurationPropertiesEntry()
                                            .key("httpClient.requests[1].methods[0].retries[0].count")
                                            .value("2"),
                                    new ConfigurationPropertiesEntry()
                                            .key("httpClient.requests[1].methods[0].retries[0].parameters.log-level")
                                            .value("DEBUG"));

                    // inputs
                    assertThat(bean.getInputs())
                            .hasSize(34)
                            .anyMatch(input -> input.getKey()
                                    .equals("httpClient.requests[0].methods[0].retries[0].parameters.timeout.value"))
                            .anyMatch(p -> p.getKey().equals("httpClient.requests[0].baseUrl.origin"));
                });
    }
}
