package com.nucleonforge.axile.master.service.convert;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.env.EnvironmentProperty;
import com.nucleonforge.axile.common.api.env.PropertyValue;
import com.nucleonforge.axile.master.api.response.EnvironmentPropertyResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link EnvironmentPropertyConverter}
 *
 * @since 02.09.2025
 * @author Nikita Kirillov
 */
class EnvironmentPropertyConverterTest {

    private final EnvironmentPropertyConverter subject = new EnvironmentPropertyConverter();

    @Test
    void testConvertHappyPath() {
        // when.
        EnvironmentPropertyResponse response = subject.convertInternal(getEnvironmentProperty());

        // then.
        assertThat(response.source()).isEqualTo("systemProperties");
        assertThat(response.value()).isEqualTo("Amazon.com Inc.");

        assertThat(response.propertySources())
                .hasSize(2)
                .extracting(EnvironmentPropertyResponse.PropertySource::name)
                .containsExactly("systemProperties", "systemEnvironment");

        EnvironmentPropertyResponse.PropertySource systemPropertiesSource =
                response.propertySources().get(0);
        assertThat(systemPropertiesSource.property()).isNotNull().satisfies(pv -> {
            assertThat(pv.value()).isEqualTo("Amazon.com Inc.");
            assertThat(pv.origin()).isNull();
        });

        EnvironmentPropertyResponse.PropertySource systemEnvironmentSource =
                response.propertySources().get(1);
        assertThat(systemEnvironmentSource.property()).isNotNull().satisfies(pv -> {
            assertThat(pv.value()).isEqualTo("Amazon");
            assertThat(pv.origin()).isNull();
        });
    }

    private EnvironmentProperty getEnvironmentProperty() {
        EnvironmentProperty.Property property = new EnvironmentProperty.Property("systemProperties", "Amazon.com Inc.");

        List<String> activeProfiles = new ArrayList<>();
        activeProfiles.add("production");

        List<String> defaultProfiles = new ArrayList<>();
        defaultProfiles.add("default");
        defaultProfiles.add("development");

        List<EnvironmentProperty.SourceEntry> sourceEntries = new ArrayList<>();
        sourceEntries.add(new EnvironmentProperty.SourceEntry("server.ports", null));
        sourceEntries.add(new EnvironmentProperty.SourceEntry("servletConfigInitParams", null));
        sourceEntries.add(
                new EnvironmentProperty.SourceEntry("systemProperties", new PropertyValue("Amazon.com Inc.", null)));
        sourceEntries.add(new EnvironmentProperty.SourceEntry("systemEnvironment", new PropertyValue("Amazon", null)));

        return new EnvironmentProperty(property, activeProfiles, defaultProfiles, sourceEntries);
    }
}
