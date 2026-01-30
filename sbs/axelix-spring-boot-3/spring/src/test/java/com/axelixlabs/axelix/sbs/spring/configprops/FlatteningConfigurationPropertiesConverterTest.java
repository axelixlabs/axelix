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

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesDescriptor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.api.ConfigPropsFeed;
import com.axelixlabs.axelix.common.api.KeyValue;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link FlatteningConfigurationPropertiesConverter}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "management.endpoint.configprops.show-values=always")
@TestPropertySource(
        properties = {
            "axelix.prop.test.tags.environment=test",
            "axelix.prop.test.tags.version=1.0.0",
            "axelix.prop.test.enabled-contexts=user-service,payment-service",
            "axelix.prop.test.http-client.requests[0].name=user-api",
            "axelix.prop.test.http-client.requests[0].base-url=https://api.users.example.com/v1",
            "axelix.prop.test.http-client.requests[0].methods[0].type=GET",
            "axelix.prop.test.http-client.requests[0].methods[0].retries[0].count=3",
            "axelix.prop.test.http-client.requests[0].methods[0].retries[0].parameters.timeout=5000",
            "axelix.prop.test.http-client.requests[0].methods[1].type=POST"
        })
@EnableConfigurationProperties(FlatteningConfigurationPropertiesConverterTest.AxelixConfigurationProperties.class)
public class FlatteningConfigurationPropertiesConverterTest {

    @Autowired
    private ConfigurationPropertiesReportEndpoint endpoint;

    @Autowired
    private ConfigurationPropertiesConverter enricher;

    @Test
    void getConfigPropsDescriptor() {
        ConfigurationPropertiesDescriptor defaultDescriptor = endpoint.configurationProperties();

        ConfigPropsFeed axelixConfPropDescriptor = enricher.convert(defaultDescriptor);

        assertThat(axelixConfPropDescriptor).isNotNull();

        assertThat(axelixConfPropDescriptor.contexts()).isNotEmpty();

        assertThat(axelixConfPropDescriptor.contexts().entrySet()).allSatisfy(entry -> {
            var beans = entry.getValue().beans().entrySet();

            assertThat(beans)
                    .filteredOn(e -> e.getValue().prefix().equals("axelix.prop.test"))
                    .singleElement()
                    .satisfies(bean -> {
                        var key = bean.getKey();
                        var value = bean.getValue();

                        // Bean
                        assertThat(key).isEqualTo(AxelixConfigurationProperties.class.getName());

                        // prefix
                        assertThat(value.prefix()).isEqualTo("axelix.prop.test");

                        // properties
                        assertThat(value.properties())
                                .containsOnly(
                                        new KeyValue("tags.environment", "test"),
                                        new KeyValue("tags.version", "1.0.0"),
                                        new KeyValue("enabledContexts[0]", "user-service"),
                                        new KeyValue("enabledContexts[1]", "payment-service"),
                                        new KeyValue("httpClient.requests[0].name", "user-api"),
                                        new KeyValue(
                                                "httpClient.requests[0].baseUrl", "https://api.users.example.com/v1"),
                                        new KeyValue("httpClient.requests[0].methods[0].type", "GET"),
                                        new KeyValue("httpClient.requests[0].methods[0].retries[0].count", "3"),
                                        new KeyValue(
                                                "httpClient.requests[0].methods[0].retries[0].parameters.timeout",
                                                "5000"),
                                        new KeyValue("httpClient.requests[0].methods[1].type", "POST"));

                        // inputs
                        assertThat(value.inputs())
                                .hasSize(20)
                                .anyMatch(
                                        p -> p.key()
                                                .equals(
                                                        "httpClient.requests[0].methods[0].retries[0].parameters.timeout.value"))
                                .anyMatch(p -> p.key().equals("httpClient.requests[0].baseUrl.origin"));
                    });
        });
    }

    @ConfigurationProperties(prefix = "axelix.prop.test")
    public record AxelixConfigurationProperties(
            Map<String, String> tags,
            List<String> enabledContexts,
            AxelixConfigurationPropertiesEndpointTest.AxelixConfigurationProperties.HttpClient httpClient) {

        public record HttpClient(
                List<AxelixConfigurationPropertiesEndpointTest.AxelixConfigurationProperties.Request> requests) {}

        public record Request(
                String name,
                String baseUrl,
                List<AxelixConfigurationPropertiesEndpointTest.AxelixConfigurationProperties.Method> methods) {}

        public record Method(
                String type,
                List<AxelixConfigurationPropertiesEndpointTest.AxelixConfigurationProperties.Retry> retries) {}

        public record Retry(Integer count, Map<String, Object> parameters) {}
    }

    @TestConfiguration
    static class DefaultDefaultConfigurationPropertiesTestConfiguration {

        @Bean
        public ConfigurationPropertiesConverter configurationPropertiesConverter() {
            return new FlatteningConfigurationPropertiesConverter();
        }
    }
}
