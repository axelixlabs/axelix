package com.nucleonforge.axile.sbs.spring.env;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import com.nucleonforge.axile.sbs.spring.configprops.ConfigurationPropertiesCache;
import com.nucleonforge.axile.sbs.spring.configprops.ConfigurationPropertiesConverter;
import com.nucleonforge.axile.sbs.spring.configprops.DefaultConfigurationPropertiesConverter;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxileEnvironmentEndpoint}.
 *
 * @since 21.10.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        args = {"--axile.env.test.prop3=fromCommandLine"},
        properties = {
            "axile.env.test.prop2=systemValue2",
            "management.endpoint.env.show-values=always",
        })
@TestPropertySource(
        properties = {
            // properties -> shouldSelectPrimaryPropertyFromHighestPrecedenceSource
            "axile.env.test.prop1=fromTestSource",

            // properties -> shouldReturnTheBeanNameThatMatchesTheConfigProps
            "axile.prop.test.tags.environment=test",
            "axile.prop.test.tags.version=1.0.0",
            "axile.prop.test.enabled-contexts=user-service,payment-service",
            "axile.prop.test.http-client.requests[0].name=user-api",
            "axile.prop.test.http-client.requests[0].base-url=https://api.users.example.com/v1",
            "axile.prop.test.http-client.requests[0].methods[0].type=GET",
            "axile.prop.test.http-client.requests[0].methods[0].retries[0].count=3",
            "axile.prop.test.http-client.requests[0].methods[0].retries[0].parameters.timeout=5000",
            "axile.prop.test.http-client.requests[0].methods[1].type=POST",
            "axile.prop.test.http-client.requests[1].name=payment-api",
            "axile.prop.test.http-client.requests[1].base-url=https://api.payments.example.com/v2",
            "axile.prop.test.http-client.requests[1].methods[0].type=PUT",
            "axile.prop.test.http-client.requests[1].methods[0].retries[0].count=2",
            "axile.prop.test.http-client.requests[1].methods[0].retries[0].parameters.log-level=DEBUG",
        })
@EnableConfigurationProperties(AxileEnvironmentEndpointTest.AxilePropTest.class)
class AxileEnvironmentEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ConfigurableEnvironment environment;

    @BeforeEach
    void before() {
        environment.getSystemProperties().put("axile.env.test.prop1", "systemValue");
        environment.getSystemProperties().put("axile.env.test.prop2", "systemValue");
        environment.getSystemProperties().put("axile.env.test.prop3", "systemValue");
    }

    @DynamicPropertySource
    static void registerDynamic(DynamicPropertyRegistry registry) {
        registry.add("axile.env.test.prop2", () -> "dynamicValue");
    }

    @ParameterizedTest(name = "Property ''{0}'' should resolve from highest-precedence source")
    @MethodSource("propertyExpectations")
    void shouldSelectPrimaryPropertyFromHighestPrecedenceSource(String propertyName, String expectedValue) {
        ResponseEntity<AxileEnvironmentEndpoint.AxileEnvironmentDescriptor> response = restTemplate.getForEntity(
                "/actuator/axile-env", AxileEnvironmentEndpoint.AxileEnvironmentDescriptor.class);

        var propertyAppearances = response.getBody().propertySources().stream()
                .flatMap(src -> src.properties().entrySet().stream()
                        .filter(e -> e.getKey().equals(propertyName))
                        .map(e -> Map.entry(src.name(), e.getValue())))
                .toList();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(propertyAppearances)
                .isNotEmpty()
                .filteredOn(e -> e.getValue().isPrimary())
                .hasSize(1)
                .first()
                .extracting(e -> e.getValue().value())
                .isEqualTo(expectedValue);
    }

    private static Stream<Arguments> propertyExpectations() {
        return Stream.of(
                Arguments.of("axile.env.test.prop1", "fromTestSource"),
                Arguments.of("axile.env.test.prop2", "dynamicValue"),
                Arguments.of("axile.env.test.prop3", "fromCommandLine"));
    }

    @Test
    void shouldReturnValidJsonStructure() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/axile-env", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        String responseBody = response.getBody();

        // We're not exactly sure about the shape of the returned json. It may and it would
        // vary depending on the CI/CD runner, on the overall environment and spring version etc.
        // So we just check the basic invariants.
        assertThat(responseBody).isNotNull();

        assertThatJson(responseBody).node("activeProfiles").isNotNull().isArray();

        assertThatJson(responseBody).node("defaultProfiles").isNotNull().isArray();

        assertThatJson(responseBody)
                .inPath("propertySources[*].properties")
                .isArray()
                .allSatisfy(properties -> assertThatJson(properties)
                        .isObject()
                        .allSatisfy((propertyName, propertyValue) -> assertThatJson(propertyValue)
                                .isObject()
                                .containsKey("isPrimary") // isPrimary flag should always present in response
                                .node("isPrimary")
                                .isBoolean()));
    }

    @ParameterizedTest
    @MethodSource("propertyName")
    void shouldReturnTheBeanNameThatMatchesTheConfigProps(String propertyName) {
        ResponseEntity<AxileEnvironmentEndpoint.AxileEnvironmentDescriptor> response = restTemplate.getForEntity(
                "/actuator/axile-env", AxileEnvironmentEndpoint.AxileEnvironmentDescriptor.class);

        var propertyAppearances = response.getBody().propertySources().stream()
                .flatMap(src -> src.properties().entrySet().stream()
                        .filter(e -> e.getKey().equals(propertyName))
                        .map(e -> Map.entry(src.name(), e.getValue())))
                .toList();

        assertThat(propertyAppearances)
                .extracting(e -> e.getValue().configPropsBeanName())
                .containsOnly(AxilePropTest.class.getName());
    }

    private static Stream<Arguments> propertyName() {
        return Stream.of(
                Arguments.of("axile.prop.test.tags.environment"),
                Arguments.of("axile.prop.test.tags.version"),
                Arguments.of("axile.prop.test.enabled-contexts"),
                Arguments.of("axile.prop.test.http-client.requests[0].name"),
                Arguments.of("axile.prop.test.http-client.requests[0].base-url"),
                Arguments.of("axile.prop.test.http-client.requests[0].methods[0].type"),
                Arguments.of("axile.prop.test.http-client.requests[0].methods[0].retries[0].count"),
                Arguments.of("axile.prop.test.http-client.requests[0].methods[0].retries[0].parameters.timeout"),
                Arguments.of("axile.prop.test.http-client.requests[0].methods[1].type"),
                Arguments.of("axile.prop.test.http-client.requests[1].name"),
                Arguments.of("axile.prop.test.http-client.requests[1].base-url"),
                Arguments.of("axile.prop.test.http-client.requests[1].methods[0].type"),
                Arguments.of("axile.prop.test.http-client.requests[1].methods[0].retries[0].count"),
                Arguments.of("axile.prop.test.http-client.requests[1].methods[0].retries[0].parameters.log-level"));
    }

    @ConfigurationProperties(prefix = "axile.prop.test")
    public record AxilePropTest(Map<String, String> tags, List<String> enabledContexts, HttpClient httpClient) {

        public record HttpClient(List<Request> requests) {}

        public record Request(String name, String baseUrl, List<Method> methods) {}

        public record Method(String type, List<Retry> retries) {}

        public record Retry(Integer count, Map<String, Object> parameters) {}
    }

    @TestConfiguration
    static class AxileEnvironmentEndpointTestConfiguration {

        @Bean
        public ConfigurationPropertiesConverter configurationPropertiesConverter() {
            return new DefaultConfigurationPropertiesConverter();
        }

        @Bean
        public ConfigurationPropertiesCache configurationPropertiesCache(
                ConfigurationPropertiesReportEndpoint configurationPropertiesReportEndpoint,
                ConfigurationPropertiesConverter configurationPropertiesConverter) {
            return new ConfigurationPropertiesCache(
                    configurationPropertiesReportEndpoint, configurationPropertiesConverter);
        }

        @Bean
        public EnvironmentPropertyNameNormalizer propertyNameNormalizer() {
            return new DefaultEnvironmentPropertyNameNormalizer();
        }

        @Bean
        public EnvPropertyEnricher envPropertyEnricher(
                Environment environment,
                EnvironmentPropertyNameNormalizer propertyNameNormalizer,
                ObjectProvider<ConfigurationPropertiesCache> configurationPropertiesCache) {
            return new DefaultEnvPropertyEnricher(environment, propertyNameNormalizer, configurationPropertiesCache);
        }

        @Bean
        public AxileEnvironmentEndpoint axileEnvironmentEndpoint(
                EnvironmentEndpoint delegate, EnvPropertyEnricher envPropertyEnricher) {
            return new AxileEnvironmentEndpoint(delegate, envPropertyEnricher);
        }
    }
}
