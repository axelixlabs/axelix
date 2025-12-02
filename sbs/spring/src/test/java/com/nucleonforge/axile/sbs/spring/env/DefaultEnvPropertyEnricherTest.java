package com.nucleonforge.axile.sbs.spring.env;

import org.junit.jupiter.api.BeforeAll;
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
import org.springframework.core.env.StandardEnvironment;

import com.nucleonforge.axile.common.api.env.EnvironmentFeed;
import com.nucleonforge.axile.common.api.env.EnvironmentFeed.PropertySource;
import com.nucleonforge.axile.sbs.spring.configprops.ConfigurationPropertiesCache;
import com.nucleonforge.axile.sbs.spring.configprops.ConfigurationPropertiesConverter;
import com.nucleonforge.axile.sbs.spring.configprops.DefaultConfigurationPropertiesConverter;
import com.nucleonforge.axile.sbs.spring.properties.utils.DefaultEnvironmentPropertyNameNormalizer;
import com.nucleonforge.axile.sbs.spring.properties.utils.EnvironmentPropertyNameNormalizer;
import com.nucleonforge.axile.sbs.spring.properties.utils.InvalidPropertiesLoader;
import com.nucleonforge.axile.sbs.spring.properties.utils.YamlInvalidPropertiesLoader;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultEnvPropertyEnricher}.
 *
 * @since 21.10.2025
 * @author Nikita Kirillov
 */
@SpringBootTest(args = "--fooBar=fromArgs")
class DefaultEnvPropertyEnricherTest {

    @Autowired
    private EnvironmentEndpoint environmentEndpoint;

    @Autowired
    private EnvPropertyEnricher enricher;

    @BeforeAll
    static void beforeAll() {
        System.setProperty("foo.bar", "system.property");
    }

    @Test
    void shouldEnrichAllPropertiesWithPrimaryField() {
        EnvironmentDescriptor defaultDescriptor = environmentEndpoint.environment(null);

        EnvironmentFeed environmentFeed = enricher.enrich(defaultDescriptor);

        assertThat(environmentFeed).isNotNull();
        assertThat(environmentFeed.activeProfiles()).isNotNull();
        assertThat(environmentFeed.defaultProfiles()).isNotNull();
        assertThat(environmentFeed.propertySources()).isNotEmpty();

        // property from the command line args should win
        // https://docs.spring.io/spring-boot/reference/features/external-config.html
        assertThat(findPropertySource(environmentFeed, StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME)
                        .properties()
                        .get("foo.bar")
                        .isPrimary())
                .isFalse();

        assertThat(findPropertySource(environmentFeed, "commandLineArgs")
                        .properties()
                        .get("fooBar")
                        .isPrimary())
                .isTrue();
    }

    private static PropertySource findPropertySource(EnvironmentFeed environmentFeed, String propertySourceName) {
        return environmentFeed.propertySources().stream()
                .filter(it -> it.sourceName().equals(propertySourceName))
                .findFirst()
                .get();
    }

    @TestConfiguration
    static class DefaultEnvPropertyEnricherTestConfiguration {

        @Bean
        public EnvironmentPropertyNameNormalizer propertyNameNormalizer() {
            return new DefaultEnvironmentPropertyNameNormalizer();
        }

        @Bean
        public InvalidPropertiesLoader invalidPropertiesLoader(
                EnvironmentPropertyNameNormalizer propertyNameNormalizer) {
            return new YamlInvalidPropertiesLoader(propertyNameNormalizer);
        }

        @Bean
        public ConfigurationPropertiesConverter configurationPropertiesConverter(
                InvalidPropertiesLoader invalidPropertiesLoader) {
            return new DefaultConfigurationPropertiesConverter(invalidPropertiesLoader);
        }

        @Bean
        public ConfigurationPropertiesCache configurationPropertiesCache(
                ConfigurationPropertiesReportEndpoint configurationPropertiesReportEndpoint,
                ConfigurationPropertiesConverter configurationPropertiesConverter) {
            return new ConfigurationPropertiesCache(
                    configurationPropertiesReportEndpoint, configurationPropertiesConverter);
        }

        @Bean
        public EnvPropertyEnricher envPropertyEnricher(
                Environment environment,
                EnvironmentPropertyNameNormalizer propertyNameNormalizer,
                ObjectProvider<ConfigurationPropertiesCache> configurationPropertiesCache,
                InvalidPropertiesLoader invalidPropertiesLoader) {
            return new DefaultEnvPropertyEnricher(
                    environment, propertyNameNormalizer, configurationPropertiesCache, invalidPropertiesLoader);
        }
    }
}
