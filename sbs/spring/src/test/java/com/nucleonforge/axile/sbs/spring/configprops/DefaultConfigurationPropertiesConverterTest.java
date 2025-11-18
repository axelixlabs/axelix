package com.nucleonforge.axile.sbs.spring.configprops;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint;
import org.springframework.boot.actuate.context.properties.ConfigurationPropertiesReportEndpoint.ConfigurationPropertiesDescriptor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import com.nucleonforge.axile.sbs.spring.configprops.ConfigurationPropertiesCache.AxileConfigurationPropertiesDescriptor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link DefaultConfigurationPropertiesConverter}.
 *
 * @author Sergey Cherkasov
 */
@SpringBootTest
public class DefaultConfigurationPropertiesConverterTest {

    @Autowired
    private ConfigurationPropertiesReportEndpoint endpoint;

    @Autowired
    private ConfigurationPropertiesConverter enricher;

    @Test
    void getConfigPropsDescriptor() {
        ConfigurationPropertiesDescriptor defaultDescriptor = endpoint.configurationProperties();

        AxileConfigurationPropertiesDescriptor axileConfPropDescriptor = enricher.convert(defaultDescriptor);

        assertThat(axileConfPropDescriptor).isNotNull();
        assertThat(axileConfPropDescriptor.contexts()).isNotEmpty();
        assertThat(axileConfPropDescriptor.contexts().entrySet()).allSatisfy(entry -> {
            assertThat(entry.getValue().beans()).isNotEmpty();

            var beans = entry.getValue().beans().entrySet();

            assertThat(beans)
                    .first()
                    .satisfies(prefix -> assertThat(prefix.getValue().prefix()).isNotNull())
                    .satisfies(
                            prefix -> assertThat(prefix.getValue().properties()).isNotEmpty())
                    .satisfies(prefix -> assertThat(prefix.getValue().inputs()).isNotEmpty());
        });
    }

    @TestConfiguration
    static class DefaultDefaultConfigurationPropertiesTestConfiguration {
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
    }
}
