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
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.common.api.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.common.api.KeyValue;
import com.axelixlabs.axelix.sbs.spring.core.configprops.AxelixConfigurationPropertiesEndpointTest.AxelixConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.configprops.AxelixConfigurationPropertiesEndpointTest.AxelixConfigurationPropertiesTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.configprops.AxelixConfigurationPropertiesEndpointTest.AxelixMutateConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.env.DefaultPropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.core.env.PropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.core.properties.DefaultPropertyNameDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.properties.PropertyNameDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.properties.SmartSanitizingFunction;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixConfigurationPropertiesEndpoint}.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 * @author Nikita Kirillov
 */
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
            "AXELIX_MUTATE_PROP_TEST_TAGS_VERSION=1.0.0",
            "AXELIX_MUTATE_PROP_TEST_ENABLED_CONTEXTS=user-service, payment-service",
            "AXELIX_MUTATE_PROP_TEST_ENABLED=true",
            "AXELIX_MUTATE_PROP_TEST_COUNT=1"
        })
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "management.endpoint.env.show-values=always")
@EnableConfigurationProperties({AxelixConfigurationProperties.class, AxelixMutateConfigurationProperties.class})
@Import(AxelixConfigurationPropertiesTestConfiguration.class)
public class AxelixConfigurationPropertiesEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @ParameterizedTest
    @MethodSource("propertyName")
    void shouldReturnPropertiesNameAndValue(String propertyName, String expectedValue) {
        ResponseEntity<ConfigurationPropertiesFeed> response =
                restTemplate.getForEntity("/actuator/axelix-configprops", ConfigurationPropertiesFeed.class);

        List<KeyValue> properties = response.getBody().getBeans().stream()
                .filter(beans -> beans.getPrefix().equals("axelix.prop.test"))
                .flatMap(bean -> bean.getProperties().stream())
                .toList();

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

    @DynamicPropertySource
    static void registerDynamic(DynamicPropertyRegistry registry) {
        registry.add("axelix.prop.test.dynamicProperties", () -> "new-dynamic-value");
    }

    @ParameterizedTest
    @MethodSource("mutateProperty")
    void mutate_shouldUpdatePropertyValue(String envProperty, String mutateProperty, String newValue) {
        mutateProperty(mutateProperty, newValue);

        Map<?, ?> updatedResponse = restTemplate.getForObject("/actuator/env/" + envProperty, Map.class);

        assertThat(updatedResponse)
                .isNotNull()
                .extracting("property")
                .isInstanceOf(Map.class)
                .extracting("value")
                .isEqualTo(newValue);
    }

    private static Stream<Arguments> mutateProperty() {
        return Stream.of(
                Arguments.of(
                        "AXELIX_MUTATE_PROP_TEST_ENABLED_CONTEXTS",
                        "axelix.mutate.prop.test.enabled-contexts",
                        "new-service, test-service"),
                Arguments.of("AXELIX_MUTATE_PROP_TEST_TAGS_VERSION", "axelix.mutate.prop.test.tags.version", "1.0.0"),
                Arguments.of("AXELIX_MUTATE_PROP_TEST_ENABLED", "axelix.mutate.prop.test.enabled", "false"),
                Arguments.of("AXELIX_MUTATE_PROP_TEST_COUNT", "axelix.mutate.prop.test.count", "1"));
    }

    @Test
    void mutate_shouldReturnBadRequest_whenNewPropertyValueIsNull() {
        ConfigurationPropertyMutationRequest request = new ConfigurationPropertyMutationRequest("property", null);

        ResponseEntity<Void> response = restTemplate.postForEntity(path(), defaultEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.BAD_REQUEST, ResponseEntity::getStatusCode);
    }

    @ParameterizedTest
    @MethodSource("emptyPropertyName")
    void mutate_shouldReturnBadRequest_whenPropertyNameIsEmpty(String emptyProperty) {
        ConfigurationPropertyMutationRequest request =
                new ConfigurationPropertyMutationRequest(emptyProperty, "someValue");

        ResponseEntity<Void> response = restTemplate.postForEntity(path(), defaultEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.BAD_REQUEST, ResponseEntity::getStatusCode);
    }

    @ParameterizedTest
    @MethodSource("invalidMutateValue")
    void mutate_shouldReturnBadRequest_whenPropertyValueHasInvalidType(String propertyName, String invalidValue) {
        ConfigurationPropertyMutationRequest request =
                new ConfigurationPropertyMutationRequest(propertyName, invalidValue);

        ResponseEntity<Void> response = restTemplate.postForEntity(path(), defaultEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.BAD_REQUEST, ResponseEntity::getStatusCode);
    }

    private static Stream<Arguments> invalidMutateValue() {
        return Stream.of(
                Arguments.of("axelix.mutate.prop.test.enabled", "maybe"),
                Arguments.of("axelix.mutate.prop.test.count", "not-a-number"));
    }

    @Test
    void mutate_shouldReturnBadRequest_whenPropertyDoesNotExist() {
        ConfigurationPropertyMutationRequest request =
                new ConfigurationPropertyMutationRequest("axelix.mutate.prop.test.unknown-field", "value");

        ResponseEntity<Void> response = restTemplate.postForEntity(path(), defaultEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.BAD_REQUEST, ResponseEntity::getStatusCode);
    }

    private static Stream<Arguments> emptyPropertyName() {
        return Stream.of(Arguments.of(""), Arguments.of(" "), Arguments.of("\t"));
    }

    private void mutateProperty(String propertyName, String newValue) {
        ConfigurationPropertyMutationRequest request = new ConfigurationPropertyMutationRequest(propertyName, newValue);

        ResponseEntity<Void> response = restTemplate.postForEntity(path(), defaultEntity(request), Void.class);

        assertThat(response).isNotNull().returns(HttpStatus.NO_CONTENT, ResponseEntity::getStatusCode);
    }

    private HttpEntity<ConfigurationPropertyMutationRequest> defaultEntity(
            ConfigurationPropertyMutationRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(request, headers);
    }

    private String path() {
        return "/actuator/axelix-configprops";
    }

    @ConfigurationProperties(prefix = "axelix.mutate.prop.test")
    public static class AxelixMutateConfigurationProperties {

        private Map<String, String> tags;

        private List<String> enabledContexts;

        private boolean enabled;

        private int count;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public Map<String, String> getTags() {
            return tags;
        }

        public void setTags(Map<String, String> tags) {
            this.tags = tags;
        }

        public List<String> getEnabledContexts() {
            return enabledContexts;
        }

        public void setEnabledContexts(List<String> enabledContexts) {
            this.enabledContexts = enabledContexts;
        }
    }

    @ConfigurationProperties(prefix = "axelix.prop.test")
    public record AxelixConfigurationProperties(
            Map<String, String> tags, List<String> enabledContexts, HttpClient httpClient) {

        public record HttpClient(List<Request> requests) {}

        public record Request(String name, String baseUrl, List<Method> methods) {}

        public record Method(String type, List<Retry> retries) {}

        public record Retry(Integer count, Map<String, Object> parameters) {}
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
        @ConditionalOnMissingBean
        public ConfigurationPropertiesCache configurationPropertiesCache(
                SmartSanitizingFunction smartSanitizingFunction,
                ApplicationContext applicationContext,
                ConfigurationPropertiesConverter configurationPropertiesConverter) {
            return new ConfigurationPropertiesCache(
                    smartSanitizingFunction, applicationContext, configurationPropertiesConverter);
        }

        @Bean
        @ConditionalOnMissingBean
        public ConfigurationPropertiesBeansCache configurationPropertiesBeansCache(
                ConfigurableApplicationContext applicationContext) {
            return new ConfigurationPropertiesBeansCache(applicationContext);
        }

        @Bean
        @ConditionalOnMissingBean
        public PropertyNameDiscoverer propertyNameDiscoverer(
                ConfigurableApplicationContext applicationContext, PropertyNameNormalizer propertyNameNormalizer) {
            return new DefaultPropertyNameDiscoverer(applicationContext, propertyNameNormalizer);
        }

        @Bean
        @ConditionalOnMissingBean
        public ConfigurationPropertiesRuntimeValidator configurationPropertiesRuntimeValidator() {
            return new ConfigurationPropertiesRuntimeValidator();
        }

        @Bean
        @ConditionalOnMissingBean
        public ConfigurationPropertiesMutabilityChecker configurationPropertiesMutabilityChecker() {
            return new ConfigurationPropertiesMutabilityChecker();
        }

        @Bean
        @ConditionalOnMissingBean
        public ConfigurationPropertiesMutator configurationPropertiesMutator(
                ConfigurableEnvironment configurableEnvironment,
                PropertyNameDiscoverer propertyNameDiscoverer,
                ConfigurationPropertiesRuntimeValidator configurationPropertiesRuntimeValidator,
                ConfigurationPropertiesBeansCache configurationPropertiesBeansCache,
                ConfigurationPropertiesMutabilityChecker configurationPropertiesMutabilityChecker) {
            return new RebindingConfigurationPropertiesMutator(
                    configurableEnvironment,
                    propertyNameDiscoverer,
                    configurationPropertiesRuntimeValidator,
                    configurationPropertiesBeansCache,
                    configurationPropertiesMutabilityChecker);
        }

        @Bean
        @ConditionalOnMissingBean
        public AxelixConfigurationPropertiesEndpoint axelixConfigurationPropertiesEndpoint(
                ConfigurationPropertiesCache configurationPropertiesCache,
                ConfigurationPropertiesMutator configurationPropertiesMutator) {
            return new AxelixConfigurationPropertiesEndpoint(
                    configurationPropertiesCache, configurationPropertiesMutator);
        }
    }
}
