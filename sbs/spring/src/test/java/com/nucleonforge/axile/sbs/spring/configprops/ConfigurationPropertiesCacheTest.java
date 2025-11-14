package com.nucleonforge.axile.sbs.spring.configprops;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesDescriptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ConfigurationPropertiesCache}.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfigurationPropertiesCacheTest {

    @Autowired
    ConfigurationPropertiesCache configurationPropertiesCache;

    @Test
    void shouldReturnConfigurationProperties() {
        assertThat(configurationPropertiesCache.getConfigurationProperties())
                .isNotNull()
                .isInstanceOf(ConfigurationPropertiesDescriptor.class);
    }

    @TestConfiguration
    static class ServiceConfigurationPropertiesTestConfiguration {

        @Bean
        public ConfigurationPropertiesCache serviceConfigurationProperties(
                ConfigurationPropertiesReportEndpoint configurationPropertiesReportEndpoint) {
            return new ConfigurationPropertiesCache(configurationPropertiesReportEndpoint);
        }

        @Bean
        public AxileConfigurationPropertiesEndpoint axileConfigurationPropertiesEndpoint(
                ConfigurationPropertiesCache configurationPropertiesCache) {
            return new AxileConfigurationPropertiesEndpoint(configurationPropertiesCache);
        }
    }
}
