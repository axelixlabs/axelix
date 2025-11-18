package com.nucleonforge.axile.sbs.spring.configprops;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import com.nucleonforge.axile.common.api.KeyValue;
import com.nucleonforge.axile.sbs.spring.configprops.ConfigurationPropertiesCache.AxileConfigurationPropertiesDescriptor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxileConfigurationPropertiesEndpoint}.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "management.endpoint.configprops.show-values=always")
@TestPropertySource(
        properties = {
            "axile.properties.test.tags.environment=test",
            "axile.properties.test.tags.version=1.0.0",
            "axile.properties.test.enabled-contexts[0]=user-service",
            "axile.properties.test.enabled-contexts[1]=payment-service",
            "axile.properties.test.http-client.requests[0].name=user-api",
            "axile.properties.test.http-client.requests[0].base-url=https://api.users.example.com/v1",
            "axile.properties.test.http-client.requests[0].methods[0].type=GET",
            "axile.properties.test.http-client.requests[0].methods[0].retries[0].count=3",
            "axile.properties.test.http-client.requests[0].methods[0].retries[0].parameters.timeout=5000",
            "axile.properties.test.http-client.requests[0].methods[1].type=POST",
            "axile.properties.test.http-client.requests[1].name=payment-api",
            "axile.properties.test.http-client.requests[1].base-url=https://api.payments.example.com/v2",
            "axile.properties.test.http-client.requests[1].methods[0].type=PUT",
            "axile.properties.test.http-client.requests[1].methods[0].retries[0].count=2",
            "axile.properties.test.http-client.requests[1].methods[0].retries[0].parameters.log-level=DEBUG",
        })
@EnableConfigurationProperties(AxileConfigurationPropertiesEndpointTest.AxilePropTest.class)
public class AxileConfigurationPropertiesEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @ParameterizedTest
    @MethodSource("propertyName")
    void shouldReturnPropertiesNameAndValue(String propertyName, String expectedValue) {
        ResponseEntity<AxileConfigurationPropertiesDescriptor> response =
                restTemplate.getForEntity("/actuator/axile-configprops", AxileConfigurationPropertiesDescriptor.class);

        List<KeyValue> properties = response.getBody().contexts().values().stream()
                .flatMap(ctx -> ctx.beans().values().stream())
                .filter(e -> e.prefix().equals("axile.properties.test"))
                .flatMap(bean -> bean.properties().stream())
                .toList();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(properties)
                .filteredOn(e -> e.key().equals(propertyName))
                .extracting(KeyValue::value)
                .containsExactly(expectedValue);
    }

    @ParameterizedTest
    @MethodSource("propertyName")
    void shouldReturnByNamePrefixPropertyNameAndValue(String propertyName, String expectedValue) {
        ResponseEntity<AxileConfigurationPropertiesDescriptor> response = restTemplate.getForEntity(
                "/actuator/axile-configprops/axile.properties.test", AxileConfigurationPropertiesDescriptor.class);

        List<KeyValue> properties = response.getBody().contexts().values().stream()
                .flatMap(ctx -> ctx.beans().values().stream())
                .flatMap(bean -> bean.properties().stream())
                .toList();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(properties)
                .filteredOn(e -> e.key().equals(propertyName))
                .extracting(KeyValue::value)
                .containsExactly(expectedValue);
    }

    private static Stream<Arguments> propertyName() {
        return Stream.of(
                Arguments.of("tags.environment", "test"),
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

    @ParameterizedTest
    @MethodSource("inputsName")
    void shouldReturnInputsName(String inputsName) {
        ResponseEntity<AxileConfigurationPropertiesDescriptor> response =
                restTemplate.getForEntity("/actuator/axile-configprops", AxileConfigurationPropertiesDescriptor.class);

        List<KeyValue> inputs = response.getBody().contexts().values().stream()
                .flatMap(ctx -> ctx.beans().values().stream())
                .filter(e -> e.prefix().equals("axile.properties.test"))
                .flatMap(bean -> bean.inputs().stream())
                .toList();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(inputs).extracting(KeyValue::key).contains(inputsName);
    }

    @ParameterizedTest
    @MethodSource("inputsName")
    void shouldReturnByNamePrefixInputsName(String inputsName) {
        ResponseEntity<AxileConfigurationPropertiesDescriptor> response = restTemplate.getForEntity(
                "/actuator/axile-configprops/axile.properties.test", AxileConfigurationPropertiesDescriptor.class);

        List<KeyValue> inputs = response.getBody().contexts().values().stream()
                .flatMap(ctx -> ctx.beans().values().stream())
                .flatMap(bean -> bean.inputs().stream())
                .toList();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(inputs).extracting(KeyValue::key).contains(inputsName);
    }

    private static Stream<Arguments> inputsName() {
        return Stream.of(
                Arguments.of("tags.environment.value"),
                Arguments.of("tags.environment.origin"),
                Arguments.of("tags.version.value"),
                Arguments.of("tags.version.origin"),
                Arguments.of("enabledContexts[0].value"),
                Arguments.of("enabledContexts[0].origin"),
                Arguments.of("enabledContexts[1].value"),
                Arguments.of("enabledContexts[1].origin"),
                Arguments.of("httpClient.requests[0].name.value"),
                Arguments.of("httpClient.requests[0].name.origin"),
                Arguments.of("httpClient.requests[0].baseUrl.value"),
                Arguments.of("httpClient.requests[0].baseUrl.origin"),
                Arguments.of("httpClient.requests[0].methods[0].type.value"),
                Arguments.of("httpClient.requests[0].methods[0].type.origin"),
                Arguments.of("httpClient.requests[0].methods[0].retries[0].count.value"),
                Arguments.of("httpClient.requests[0].methods[0].retries[0].count.origin"),
                Arguments.of("httpClient.requests[0].methods[0].retries[0].parameters.timeout.value"),
                Arguments.of("httpClient.requests[0].methods[0].retries[0].parameters.timeout.origin"),
                Arguments.of("httpClient.requests[0].methods[1].type.value"),
                Arguments.of("httpClient.requests[0].methods[1].type.origin"),
                Arguments.of("httpClient.requests[1].name.value"),
                Arguments.of("httpClient.requests[1].name.origin"),
                Arguments.of("httpClient.requests[1].baseUrl.value"),
                Arguments.of("httpClient.requests[1].baseUrl.origin"),
                Arguments.of("httpClient.requests[1].methods[0].type.value"),
                Arguments.of("httpClient.requests[1].methods[0].type.origin"),
                Arguments.of("httpClient.requests[1].methods[0].retries[0].count.value"),
                Arguments.of("httpClient.requests[1].methods[0].retries[0].count.origin"),
                Arguments.of("httpClient.requests[1].methods[0].retries[0].parameters.log-level.value"),
                Arguments.of("httpClient.requests[1].methods[0].retries[0].parameters.log-level.origin"));
    }

    @ConfigurationProperties(prefix = "axile.properties.test")
    public record AxilePropTest(Map<String, String> tags, List<String> enabledContexts, HttpClient httpClient) {

        public record HttpClient(List<Request> requests) {}

        public record Request(String name, String baseUrl, List<Method> methods) {}

        public record Method(String type, List<Retry> retries) {}

        public record Retry(Integer count, Map<String, Object> parameters) {}
    }

    @TestConfiguration
    static class AxileConfigurationPropertiesTestConfiguration {

        @Bean
        public ConfigurationPropertiesConverter configurationPropertiesConverter() {
            return new DefaultConfigurationPropertiesConverter();
        }

        @Bean
        public ConfigurationPropertiesCache serviceConfigurationProperties(
                ConfigurationPropertiesReportEndpoint configurationPropertiesReportEndpoint,
                ConfigurationPropertiesConverter configurationPropertiesConverter) {
            return new ConfigurationPropertiesCache(
                    configurationPropertiesReportEndpoint, configurationPropertiesConverter);
        }

        @Bean
        public AxileConfigurationPropertiesEndpoint axileConfigurationPropertiesEndpoint(
                ConfigurationPropertiesCache configurationPropertiesCache) {
            return new AxileConfigurationPropertiesEndpoint(configurationPropertiesCache);
        }
    }
}
