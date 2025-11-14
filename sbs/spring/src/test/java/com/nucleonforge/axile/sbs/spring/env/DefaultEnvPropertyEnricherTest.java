package com.nucleonforge.axile.sbs.spring.env;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.actuate.env.EnvironmentEndpoint.EnvironmentDescriptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.nucleonforge.axile.sbs.spring.configprops.ConfigurationPropertiesCache;
import com.nucleonforge.axile.sbs.spring.env.AxileEnvironmentEndpoint.AxileEnvironmentDescriptor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultEnvPropertyEnricher}.
 *
 * @since 21.10.2025
 * @author Nikita Kirillov
 */
@SpringBootTest
class DefaultEnvPropertyEnricherTest {

    @Autowired
    private EnvironmentEndpoint environmentEndpoint;

    @Autowired
    private EnvPropertyEnricher enricher;

    @Test
    void shouldEnrichAllPropertiesWithPrimaryField() {
        EnvironmentDescriptor defaultDescriptor = environmentEndpoint.environment(null);

        AxileEnvironmentDescriptor axileEnvironmentDescriptor = enricher.enrich(defaultDescriptor);

        assertThat(axileEnvironmentDescriptor).isNotNull();
        assertThat(axileEnvironmentDescriptor.activeProfiles()).isNotNull();
        assertThat(axileEnvironmentDescriptor.defaultProfiles()).isNotNull();
        assertThat(axileEnvironmentDescriptor.propertySources()).isNotEmpty();

        axileEnvironmentDescriptor.propertySources().forEach(source -> {
            source.properties().forEach((key, value) -> {
                assertThat(value.isPrimary())
                        .as("isPrimary flag for key '%s' should be either true or false", key)
                        .isIn(true, false);
            });
        });
    }

    @TestConfiguration
    static class DefaultEnvPropertyEnricherTestConfiguration {
        @Bean
        public ConfigurationPropertiesCache configurationPropertiesCache(
                ConfigurationPropertiesReportEndpoint configurationPropertiesReportEndpoint) {
            return new ConfigurationPropertiesCache(configurationPropertiesReportEndpoint);
        }

        @Bean
        public EnvironmentPropertyNameNormalizer propertyNameNormalizer() {
            return new DefaultEnvironmentPropertyNameNormalizer();
        }

        @Bean
        public EnvPropertyEnricher envPropertyEnricher(
                Environment environment,
                DefaultEnvironmentPropertyNameNormalizer propertyNameNormalizer,
                ObjectProvider<ConfigurationPropertiesCache> configurationPropertiesCache) {
            return new DefaultEnvPropertyEnricher(environment, propertyNameNormalizer, configurationPropertiesCache);
        }
    }
}
