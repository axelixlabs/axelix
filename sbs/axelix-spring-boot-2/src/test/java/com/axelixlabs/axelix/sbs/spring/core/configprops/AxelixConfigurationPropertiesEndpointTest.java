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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.api.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.api.KeyValue;
import com.axelixlabs.axelix.sbs.spring.core.env.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.core.env.PropertyNameNormalizer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixConfigurationPropertiesEndpoint}.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
            "axelix.prop.test.tags.forSanitization=toBeSanitized",
            "axelix.prop.test.tags.FOR_SANITIZATION=toBeSanitized",
            "axelix.prop.test.tags.version=1.0.0",
            "axelix.prop.test.enabled-contexts=user-service, payment-service",
            "axelix.prop.test.http-client.requests[0].name=user-api",
            "axelix.prop.test.http-client.requests[0].base-url=https://api.users.example.com/v1",
            "axelix.prop.test.http-client.requests[0].methods[0].type=GET",
            "axelix.prop.test.http-client.requests[0].methods[0].retries[0].count=3",
            "axelix.prop.test.http-client.requests[0].methods[0].retries[0].parameters.timeout=5000",
            "axelix.prop.test.http-client.requests[0].methods[1].type=POST",
            "axelix.prop.test.http-client.requests[1].name=payment-api",
            "axelix.prop.test.http-client.requests[1].base-url=https://api.payments.example.com/v2",
            "axelix.prop.test.http-client.requests[1].methods[0].type=PUT",
            "axelix.prop.test.http-client.requests[1].methods[0].retries[0].count=2",
            "axelix.prop.test.http-client.requests[1].methods[0].retries[0].parameters.log-level=DEBUG",
        })
@EnableConfigurationProperties({AxelixConfigurationPropertiesEndpointTest.AxelixConfigurationProperties.class})
public class AxelixConfigurationPropertiesEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @ParameterizedTest
    @MethodSource("propertyName")
    void shouldReturnPropertiesNameAndValue(String propertyName, String expectedValue) {
        ResponseEntity<ConfigurationPropertiesFeed> response =
                restTemplate.getForEntity("/actuator/axelix-configprops", ConfigurationPropertiesFeed.class);

        List<KeyValue> properties = response.getBody().getBeans().stream()
                .filter(bean -> bean.getPrefix().equals("axelix.prop.test"))
                .flatMap(bean -> bean.getProperties().stream())
                .collect(Collectors.toList());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(properties)
                .filteredOn(e -> e.getKey().equals(propertyName))
                .extracting(KeyValue::getValue)
                .containsExactly(expectedValue);
    }

    private static Stream<Arguments> propertyName() {
        return Stream.of(
                Arguments.of("tags.forSanitization", "******"),
                Arguments.of("tags.FOR_SANITIZATION", "******"),
                Arguments.of("tags.version", "1.0.0"),
                Arguments.of("enabledContexts[0]", "user-service"),
                Arguments.of("enabledContexts[1]", "payment-service"),
                Arguments.of("httpClient.requests[0].name", "user-api"),
                Arguments.of("httpClient.requests[0].baseUrl", "https://api.users.example.com/v1"),
                Arguments.of("httpClient.requests[0].methods[0].type", "GET"),
                Arguments.of("httpClient.requests[0].methods[0].retries[0].count", "3"),
                Arguments.of("httpClient.requests[0].methods[0].retries[0].parameters.timeout", "5000"),
                Arguments.of("httpClient.requests[0].methods[1].type", "POST"),
                Arguments.of("httpClient.requests[1].name", "payment-api"),
                Arguments.of("httpClient.requests[1].baseUrl", "https://api.payments.example.com/v2"),
                Arguments.of("httpClient.requests[1].methods[0].type", "PUT"),
                Arguments.of("httpClient.requests[1].methods[0].retries[0].count", "2"),
                Arguments.of("httpClient.requests[1].methods[0].retries[0].parameters.log-level", "DEBUG"));
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
    static class AxelixConfigurationPropertiesTestConfiguration {

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
        public PropertyNameNormalizer propertyNameNormalizer() {
            return new DefaultPropertyNameNormalizer();
        }

        @Bean
        public SmartSanitizingFunction smartSanitizingFunction(PropertyNameNormalizer propertyNameNormalizer) {
            return new SmartSanitizingFunction(
                    List.of("axelix.prop.test.tags.forSanitization", "axelix.prop.test.tags.FOR_SANITIZATION"),
                    propertyNameNormalizer);
        }

        @Bean
        public ConfigurationPropertiesCache configurationPropertiesCache(
                SmartSanitizingFunction smartSanitizingFunction,
                ApplicationContext applicationContext,
                ConfigurationPropertiesConverter configurationPropertiesConverter) {
            return new ConfigurationPropertiesCache(
                    smartSanitizingFunction, applicationContext, configurationPropertiesConverter);
        }

        @Bean
        public AxelixConfigurationPropertiesEndpoint axelixConfigurationPropertiesEndpoint(
                ConfigurationPropertiesCache configurationPropertiesCache) {
            return new AxelixConfigurationPropertiesEndpoint(configurationPropertiesCache);
        }
    }
}
