package com.nucleonforge.axile.sbs.spring.configprops;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxileConfigurationPropertiesEndpoint}.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AxileConfigurationPropertiesEndpointTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnStatusOK() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/axile-configprops", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @TestConfiguration
    static class AxileConfigurationPropertiesTestConfiguration {

        @Bean
        public ServiceConfigurationProperties serviceConfigurationProperties(
                ConfigurationPropertiesReportEndpoint configurationPropertiesReportEndpoint) {
            return new ServiceConfigurationProperties(configurationPropertiesReportEndpoint);
        }

        @Bean
        public AxileConfigurationPropertiesEndpoint axileConfigurationPropertiesEndpoint(
                ServiceConfigurationProperties serviceConfigurationProperties) {
            return new AxileConfigurationPropertiesEndpoint(serviceConfigurationProperties);
        }
    }
}
