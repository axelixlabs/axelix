package com.nucleonforge.axile.sbs.spring.build;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.domain.JvmNonStandardOption;
import com.nucleonforge.axile.common.domain.JvmNonStandardOptions;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DefaultNonStandardVMOptionsDiscoverer}.
 *
 * @since 25.08.2025
 * @author Nikita Kirillov
 */
class DefaultNonStandardVMOptionsDiscovererTest {

    private final DefaultNonStandardVMOptionsDiscoverer discoverer = new DefaultNonStandardVMOptionsDiscoverer();

    @Test
    void shouldReturnNonStandardVMOptions() {
        JvmNonStandardOptions options = discoverer.discover();

        Set<String> actualOptions = options.getNonStandardOptions().stream()
                .map(JvmNonStandardOption::option)
                .collect(Collectors.toSet());

        // Use `contains` instead of `containsOnly` because Gradle may add extra JVM options during the build.
        assertThat(actualOptions).contains("-Xms256m", "-Xmx512m", "-XX:+UseG1GC");
    }
}
