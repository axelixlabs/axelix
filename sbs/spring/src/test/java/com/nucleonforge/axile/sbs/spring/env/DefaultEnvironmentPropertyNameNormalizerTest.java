package com.nucleonforge.axile.sbs.spring.env;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DefaultEnvironmentPropertyNameNormalizer}
 *
 * @author Sergey Cherkasov
 */
public class DefaultEnvironmentPropertyNameNormalizerTest {
    private final EnvironmentPropertyNameNormalizer nameNormalizer = new DefaultEnvironmentPropertyNameNormalizer();

    @Test
    void shouldNormalizePropertyNames() {
        Map<String, String> properties = Map.of(
                "spring.jpa.database-platform", "springjpadatabaseplatform",
                "spring.jpa.databasePlatform", "springjpadatabaseplatform",
                "spring.JPA.database_platform", "springjpadatabaseplatform",
                "spring.my-example.url[0]", "springmyexampleurl0",
                "spring.my-example.url[0][1]", "springmyexampleurl01",
                "MY_FOO_1_", "myfoo1",
                "MY_FOO_1", "myfoo1",
                "MY_FOO_1_2_", "myfoo12",
                "MY_FOO_1_2", "myfoo12",
                "MY_FOO_1_BAR", "myfoo1bar");

        assertThat(properties).allSatisfy((key, value) -> {
            String normalized = nameNormalizer.normalize(key);
            assertThat(normalized).isEqualTo(value);
        });
    }
}
