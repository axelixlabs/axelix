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
import java.util.Objects;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ApplicationConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.api.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.api.KeyValue;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultConfigurationPropertiesConverter}.
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
@EnableConfigurationProperties(DefaultConfigurationPropertiesConverterTest.AxelixConfigurationProperties.class)
public class DefaultConfigurationPropertiesConverterTest {

    @Autowired
    private ConfigurationPropertiesReportEndpoint endpoint;

    @Autowired
    private ConfigurationPropertiesConverter enricher;

    @Test
    void shouldReturnConfigurationPropertiesFeed() {
        ApplicationConfigurationProperties defaultDescriptor = endpoint.configurationProperties();

        ConfigurationPropertiesFeed axelixConfPropDescriptor = enricher.convert(defaultDescriptor);

        assertThat(axelixConfPropDescriptor).isNotNull();

        assertThat(axelixConfPropDescriptor.getBeans()).isNotEmpty();

        assertThat(axelixConfPropDescriptor.getBeans())
                .filteredOn(bean -> bean.getPrefix().equals("axelix.prop.test"))
                .singleElement()
                .satisfies(bean -> {
                    // Bean Name
                    assertThat(bean.getBeanName()).isEqualTo(AxelixConfigurationProperties.class.getName());

                    // prefix
                    assertThat(bean.getPrefix()).isEqualTo("axelix.prop.test");

                    // properties
                    assertThat(bean.getProperties())
                            .containsOnly(
                                    new KeyValue("tags.environment", "test"),
                                    new KeyValue("tags.version", "1.0.0"),
                                    new KeyValue("enabledContexts[0]", "user-service"),
                                    new KeyValue("enabledContexts[1]", "payment-service"),
                                    new KeyValue("httpClient.requests[0].name", "user-api"),
                                    new KeyValue("httpClient.requests[0].baseUrl", "https://api.users.example.com/v1"),
                                    new KeyValue("httpClient.requests[0].methods[0].type", "GET"),
                                    new KeyValue("httpClient.requests[0].methods[0].retries[0].count", "3"),
                                    new KeyValue(
                                            "httpClient.requests[0].methods[0].retries[0].parameters.timeout", "5000"),
                                    new KeyValue("httpClient.requests[0].methods[1].type", "POST"));

                    // inputs
                    assertThat(bean.getInputs())
                            .hasSize(20)
                            .anyMatch(input -> input.getKey()
                                    .equals("httpClient.requests[0].methods[0].retries[0].parameters.timeout.value"))
                            .anyMatch(p -> p.getKey().equals("httpClient.requests[0].baseUrl.origin"));
                });
    }

    @ConstructorBinding
    @ConfigurationProperties(prefix = "axelix.prop.test")
    public static final class AxelixConfigurationProperties {
        private final Map<String, String> tags;
        private final List<String> enabledContexts;
        private final HttpClient httpClient;

        public AxelixConfigurationProperties(
                Map<String, String> tags, List<String> enabledContexts, HttpClient httpClient) {
            this.tags = tags;
            this.enabledContexts = enabledContexts;
            this.httpClient = httpClient;
        }

        public Map<String, String> getTags() {
            return tags;
        }

        public List<String> getEnabledContexts() {
            return enabledContexts;
        }

        public HttpClient getHttpClient() {
            return httpClient;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            var that = (AxelixConfigurationProperties) obj;
            return Objects.equals(this.tags, that.tags)
                    && Objects.equals(this.enabledContexts, that.enabledContexts)
                    && Objects.equals(this.httpClient, that.httpClient);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tags, enabledContexts, httpClient);
        }

        @Override
        public String toString() {
            return "AxelixConfigurationProperties[" + "tags="
                    + tags + ", " + "enabledContexts="
                    + enabledContexts + ", " + "httpClient="
                    + httpClient + ']';
        }

        @ConstructorBinding
        public static final class HttpClient {
            private final List<Request> requests;

            public HttpClient(List<Request> requests) {
                this.requests = requests;
            }

            public List<Request> getRequests() {
                return requests;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (HttpClient) obj;
                return Objects.equals(this.requests, that.requests);
            }

            @Override
            public int hashCode() {
                return Objects.hash(requests);
            }

            @Override
            public String toString() {
                return "HttpClient[" + "requests=" + requests + ']';
            }
        }

        @ConstructorBinding
        public static final class Request {
            private final String name;
            private final String baseUrl;
            private final List<Method> methods;

            public Request(String name, String baseUrl, List<Method> methods) {
                this.name = name;
                this.baseUrl = baseUrl;
                this.methods = methods;
            }

            public String getName() {
                return name;
            }

            public String getBaseUrl() {
                return baseUrl;
            }

            public List<Method> getMethods() {
                return methods;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (Request) obj;
                return Objects.equals(this.name, that.name)
                        && Objects.equals(this.baseUrl, that.baseUrl)
                        && Objects.equals(this.methods, that.methods);
            }

            @Override
            public int hashCode() {
                return Objects.hash(name, baseUrl, methods);
            }

            @Override
            public String toString() {
                return "Request[" + "name=" + name + ", " + "baseUrl=" + baseUrl + ", " + "methods=" + methods + ']';
            }
        }

        @ConstructorBinding
        public static final class Method {
            private final String type;
            private final List<Retry> retries;

            public Method(String type, List<Retry> retries) {
                this.type = type;
                this.retries = retries;
            }

            public String getType() {
                return type;
            }

            public List<Retry> getRetries() {
                return retries;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (Method) obj;
                return Objects.equals(this.type, that.type) && Objects.equals(this.retries, that.retries);
            }

            @Override
            public int hashCode() {
                return Objects.hash(type, retries);
            }

            @Override
            public String toString() {
                return "Method[" + "type=" + type + ", " + "retries=" + retries + ']';
            }
        }

        @ConstructorBinding
        public static final class Retry {
            private final Integer count;
            private final Map<String, Object> parameters;

            public Retry(Integer count, Map<String, Object> parameters) {
                this.count = count;
                this.parameters = parameters;
            }

            public Integer getCount() {
                return count;
            }

            public Map<String, Object> getParameters() {
                return parameters;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == this) {
                    return true;
                }
                if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
                }
                var that = (Retry) obj;
                return Objects.equals(this.count, that.count) && Objects.equals(this.parameters, that.parameters);
            }

            @Override
            public int hashCode() {
                return Objects.hash(count, parameters);
            }

            @Override
            public String toString() {
                return "Retry[" + "count=" + count + ", " + "parameters=" + parameters + ']';
            }
        }
    }

    @TestConfiguration
    static class DefaultDefaultConfigurationPropertiesTestConfiguration {

        @Bean
        public ConfigurationPropertiesFlattener configurationPropertiesFlattener() {
            return new DefaultConfigurationPropertiesFlattener();
        }

        @Bean
        public ConfigurationPropertiesConverter configurationPropertiesConverter(
                ConfigurationPropertiesFlattener configurationPropertiesFlattener) {
            return new DefaultConfigurationPropertiesConverter(configurationPropertiesFlattener);
        }
    }
}
