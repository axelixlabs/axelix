package com.nucleonforge.axile.sbs.spring.properties;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import com.nucleonforge.axile.sbs.spring.env.DefaultEnvironmentPropertyNameNormalizer;
import com.nucleonforge.axile.sbs.spring.env.EnvironmentPropertyNameNormalizer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultPropertyNameDiscoverer}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(
        properties = {
            "axile.prop.test.dynamic-properties=dynamicValue",
            "axile.prop.test.enabled-contexts=user-service, payment-service",
            "axile.prop.test.http-client.requests[0].name=user-api",
            "axile.prop.test.http-client.requests[0].base-url=https://api.users.example.com/v1",
            "axile.prop.test.http-client.requests[0].methods[0].type=GET",
            "axile.prop.test.http-client.requests[0].methods[0].retries[0].count=3",
            "axile.prop.test.http-client.requests[0].methods[0].retries[0].parameters.timeout=5000",
            "AXILE_PROP_TEST_HTTP_CLIENT_REQUESTS_0_METHODS_1_TYPE=POST",
            "axile.prop.test.http-client.requests[1].name=payment-api",
            "axile.prop.test.http-client.requests[1].base-url=https://api.payments.example.com/v2",
            "axile.prop.test.http-client.requests[1].methods[0].type=PUT",
            "axile.prop.test.http-client.requests[1].methods[0].retries[0].count=2",
            "axile.prop.test.http-client.requests[1].methods[0].retries[0].parameters.log-level=DEBUG",
        })
public class DefaultPropertyNameDiscovererTest {

    @Autowired
    private PropertyNameDiscoverer discoverer;

    @DynamicPropertySource
    static void registerDynamic(DynamicPropertyRegistry registry) {
        registry.add("axile.prop.test.dynamicProperties", () -> "dynamicValue");
    }

    @ParameterizedTest
    @MethodSource("propertyName")
    void should(String discoverProperty, String expectedProperty) {
        assertThat(discoverer.discover(discoverProperty)).isEqualTo(expectedProperty);
    }

    private static Stream<Arguments> propertyName() {
        return Stream.of(
                Arguments.of("axile.property.not-found", null),
                Arguments.of("axile.prop.test.dynamic-properties", "axile.prop.test.dynamicProperties"),
                Arguments.of("AXILE_PROP_TEST_ENABLED_CONTEXTS", "axile.prop.test.enabled-contexts"),
                Arguments.of(
                        "axile.prop.test.httpClient.requests.name", "axile.prop.test.http-client.requests[0].name"),
                Arguments.of(
                        "axile.prop.test.httpClient.requests.baseUrl",
                        "axile.prop.test.http-client.requests[0].base-url"),
                Arguments.of(
                        "axile.prop.test.httpClient.requests.methods.type",
                        "axile.prop.test.http-client.requests[0].methods[0].type"),
                Arguments.of(
                        "axile.prop.test.http-client.requests[0].methods[0].retries[0].count",
                        "axile.prop.test.http-client.requests[0].methods[0].retries[0].count"),
                Arguments.of(
                        "axile.prop.test.httpClient.requests.methods.retries.parameters.timeout",
                        "axile.prop.test.http-client.requests[0].methods[0].retries[0].parameters.timeout"),
                Arguments.of(
                        "AXILE_PROP_TEST_HTTP_CLIENT_REQUESTS_METHODS_1_TYPE",
                        "AXILE_PROP_TEST_HTTP_CLIENT_REQUESTS_0_METHODS_1_TYPE"),
                Arguments.of(
                        "axile.prop.test.httpClient.requests[1].name", "axile.prop.test.http-client.requests[1].name"),
                Arguments.of(
                        "axile.prop.test.http-client.requests[1].base-url",
                        "axile.prop.test.http-client.requests[1].base-url"),
                Arguments.of(
                        "axile.prop.test.httpClient.requests[1].methods.type",
                        "axile.prop.test.http-client.requests[1].methods[0].type"),
                Arguments.of(
                        "axile.prop.test.http-client.requests[1].methods.retries[0].count",
                        "axile.prop.test.http-client.requests[1].methods[0].retries[0].count"),
                Arguments.of(
                        "axile.prop.test.httpClient.requests[1].methods.retries.parameters.logLevel",
                        "axile.prop.test.http-client.requests[1].methods[0].retries[0].parameters.log-level"));
    }

    @TestConfiguration
    static class DefaultPropertyDiscovererTestConfiguration {

        @Bean
        public EnvironmentPropertyNameNormalizer propertyNameNormalizer() {
            return new DefaultEnvironmentPropertyNameNormalizer();
        }

        @Bean
        public PropertyNameDiscoverer propertyNameDiscoverer(
                ConfigurableEnvironment configurableEnvironment,
                EnvironmentPropertyNameNormalizer propertyNameNormalizer) {
            return new DefaultPropertyNameDiscoverer(configurableEnvironment, propertyNameNormalizer);
        }
    }
}
