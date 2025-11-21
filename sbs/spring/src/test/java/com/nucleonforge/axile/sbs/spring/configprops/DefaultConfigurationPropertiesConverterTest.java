package com.nucleonforge.axile.sbs.spring.configprops;

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

import com.nucleonforge.axile.common.api.AxileConfigPropsFeed;
import com.nucleonforge.axile.common.api.KeyValue;

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
            "axile.prop.test.tags.environment=test",
            "axile.prop.test.tags.version=1.0.0",
            "axile.prop.test.enabled-contexts=user-service,payment-service",
            "axile.prop.test.http-client.requests[0].name=user-api",
            "axile.prop.test.http-client.requests[0].base-url=https://api.users.example.com/v1",
            "axile.prop.test.http-client.requests[0].methods[0].type=GET",
            "axile.prop.test.http-client.requests[0].methods[0].retries[0].count=3",
            "axile.prop.test.http-client.requests[0].methods[0].retries[0].parameters.timeout=5000",
            "axile.prop.test.http-client.requests[0].methods[1].type=POST"
        })
@EnableConfigurationProperties(DefaultConfigurationPropertiesConverterTest.AxileConfigurationProperties.class)
public class DefaultConfigurationPropertiesConverterTest {

    @Autowired
    private ConfigurationPropertiesReportEndpoint endpoint;

    @Autowired
    private ConfigurationPropertiesConverter enricher;

    @Test
    void getConfigPropsDescriptor() {
        ConfigurationPropertiesDescriptor defaultDescriptor = endpoint.configurationProperties();

        AxileConfigPropsFeed axileConfPropDescriptor = enricher.convert(defaultDescriptor);

        assertThat(axileConfPropDescriptor).isNotNull();

        assertThat(axileConfPropDescriptor.contexts()).isNotEmpty();

        assertThat(axileConfPropDescriptor.contexts().entrySet()).allSatisfy(entry -> {
            var beans = entry.getValue().beans().entrySet();

            assertThat(beans)
                    .filteredOn(e -> e.getValue().prefix().equals("axile.prop.test"))
                    .singleElement()
                    .satisfies(bean -> {
                        var key = bean.getKey();
                        var value = bean.getValue();

                        // Bean
                        assertThat(key).isEqualTo(AxileConfigurationProperties.class.getName());

                        // prefix
                        assertThat(value.prefix()).isEqualTo("axile.prop.test");

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

    @ConfigurationProperties(prefix = "axile.prop.test")
    public record AxileConfigurationProperties(
            Map<String, String> tags,
            List<String> enabledContexts,
            AxileConfigurationPropertiesEndpointTest.AxileConfigurationProperties.HttpClient httpClient) {

        public record HttpClient(
                List<AxileConfigurationPropertiesEndpointTest.AxileConfigurationProperties.Request> requests) {}

        public record Request(
                String name,
                String baseUrl,
                List<AxileConfigurationPropertiesEndpointTest.AxileConfigurationProperties.Method> methods) {}

        public record Method(
                String type,
                List<AxileConfigurationPropertiesEndpointTest.AxileConfigurationProperties.Retry> retries) {}

        public record Retry(Integer count, Map<String, Object> parameters) {}
    }

    @TestConfiguration
    static class DefaultDefaultConfigurationPropertiesTestConfiguration {

        @Bean
        public ConfigurationPropertiesConverter configurationPropertiesConverter() {
            return new DefaultConfigurationPropertiesConverter();
        }
    }
}
