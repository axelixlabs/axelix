/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.core.configprops;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.test.context.TestPropertySource;

import com.axelixlabs.axelix.sbs.spring.core.configprops.AxelixConfigurationPropertiesEndpointTest.AxelixConfigurationPropertiesTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.properties.AxelixPropertySource;

import static com.axelixlabs.axelix.sbs.spring.core.properties.AxelixPropertySource.AXELIX_PROPERTY_SOURCE_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link ConfigurationPropertiesMutator}.
 *
 * @author Nikita Kirillov
 */
@SpringBootTest
@Import(AxelixConfigurationPropertiesTestConfiguration.class)
@EnableConfigurationProperties(RebindingConfigurationPropertiesMutatorTest.AxelixMutateConfigurationProperties.class)
@TestPropertySource(
        properties = {
            "axelix.mutate.prop.test.tags.version=1.0.0",
            "axelix.mutate.prop.test.enabled-contexts=user-service, payment-service",
            "axelix.mutate.prop.test.enabled=true"
        })
class RebindingConfigurationPropertiesMutatorTest {

    @Autowired
    private ConfigurationPropertiesMutator subject;

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired
    private AxelixMutateConfigurationProperties axelixMutateConfigurationProperties;

    @Test
    void mutate_shouldRebindBooleanProperty() {
        subject.mutate("axelix.mutate.prop.test.enabled", "false");

        assertThat(axelixMutateConfigurationProperties.isEnabled()).isFalse();

        PropertySource<?> propertySource = environment.getPropertySources().get(AXELIX_PROPERTY_SOURCE_NAME);
        assertThat(propertySource).isInstanceOf(AxelixPropertySource.class);
        assertThat(propertySource.getProperty("axelix.mutate.prop.test.enabled"))
                .isEqualTo("false");
    }

    @Test
    void mutate_shouldRebindListProperty() {
        assertThat(axelixMutateConfigurationProperties.getEnabledContexts())
                .containsExactly("user-service", "payment-service");

        subject.mutate("axelix.mutate.prop.test.enabled-contexts", "new-service,test-service");

        assertThat(axelixMutateConfigurationProperties.getEnabledContexts())
                .containsExactly("new-service", "test-service");
    }

    @ParameterizedTest
    @CsvSource({"axelix.mutate.prop.test.count, not-a-number", "axelix.mutate.prop.test.enabled, maybe"})
    void mutate_shouldThrowValidationExceptionWhenValueHasInvalidType(String propertyName, String invalidValue) {
        assertThatThrownBy(() -> subject.mutate(propertyName, invalidValue))
                .isInstanceOf(ConfigurationPropertyValidationException.class);
    }

    @Test
    void mutate_shouldThrowValidationExceptionWhenPropertyDoesNotExist() {
        assertThatThrownBy(() -> subject.mutate("axelix.mutate.prop.test.unknown-field", "value"))
                .isInstanceOf(ConfigurationPropertyValidationException.class);
    }

    @Test
    void mutate_shouldThrowBindingExceptionWhenCanonicalPropertyNameCannotBeDiscovered() {
        assertThatThrownBy(() -> subject.mutate("axelix.mutate.prop.test.tags.new-key", "value"))
                .isInstanceOf(ConfigurationPropertyBindingException.class)
                .hasMessageContaining("Unable to discover actual property name");
    }

    @Test
    void mutate_shouldThrowValidationExceptionForSpringInternalConfigurationProperties() {
        assertThatThrownBy(() -> subject.mutate("spring.jackson.date-format", "yyyy-MM-dd"))
                .isInstanceOf(ConfigurationPropertyValidationException.class)
                .hasMessageContaining("Only application-specific configuration properties are mutable");
    }

    @ConfigurationProperties(prefix = "axelix.mutate.prop.test")
    public static class AxelixMutateConfigurationProperties {

        private Map<String, String> tags;

        private List<String> enabledContexts;

        private boolean enabled;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Map<String, String> getTags() {
            return tags;
        }

        public void setTags(Map<String, String> tags) {
            this.tags = tags;
        }

        public List<String> getEnabledContexts() {
            return enabledContexts;
        }

        public void setEnabledContexts(List<String> enabledContexts) {
            this.enabledContexts = enabledContexts;
        }
    }
}
