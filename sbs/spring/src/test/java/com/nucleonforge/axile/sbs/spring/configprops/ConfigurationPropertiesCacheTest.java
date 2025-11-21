package com.nucleonforge.axile.sbs.spring.configprops;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.common.api.AxileConfigPropsFeed;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link ConfigurationPropertiesCache}.
 *
 * @since 13.11.2025
 * @author Sergey Cherkasov
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "axile.prop.test.property-name=test")
@EnableConfigurationProperties(ConfigurationPropertiesCacheTest.AxileConfigurationProperties.class)
public class ConfigurationPropertiesCacheTest {

    @Autowired
    ConfigurationPropertiesCache configurationPropertiesCache;

    @Test
    void shouldReturnConfigurationProperties() {
        assertThat(configurationPropertiesCache.getAxileConfigProps())
                .isNotNull()
                .isInstanceOf(AxileConfigPropsFeed.class);

        assertThat(configurationPropertiesCache.getAxileConfigPropsByPrefix("axile.prop.test"))
                .isNotNull()
                .isInstanceOf(AxileConfigPropsFeed.class);
    }

    @ConfigurationProperties(prefix = "axile.prop.test")
    public record AxileConfigurationProperties(String propertyName) {}

    @TestConfiguration
    static class ConfigurationPropertiesCacheTestConfiguration {

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
    }
}
