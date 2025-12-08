/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.sbs.spring.env;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link DefaultPropertyNameNormalizer}
 *
 * @author Sergey Cherkasov
 */
public class DefaultPropertyNameNormalizerTest {
    private final PropertyNameNormalizer nameNormalizer = new DefaultPropertyNameNormalizer();

    @Test
    void shouldNormalizePropertyNames() {
        Map<String, String> properties = new HashMap<>();
        properties.put("spring.jpa.database-platform", "springjpadatabaseplatform");
        properties.put("spring.jpa.databasePlatform", "springjpadatabaseplatform");
        properties.put("spring.JPA.database_platform", "springjpadatabaseplatform");
        properties.put("spring.my-example.url[0]", "springmyexampleurl");
        properties.put("spring.my-example.url[0][1]", "springmyexampleurl1");
        properties.put("spring.my-example.url[10]", "springmyexampleurl10");
        properties.put("MY_FOO_1_", "myfoo1");
        properties.put("MY_FOO_1", "myfoo1");
        properties.put("MY_FOO_1_2_", "myfoo12");
        properties.put("MY_FOO_1_20_", "myfoo120");
        properties.put("MY_FOO_1_2", "myfoo12");
        properties.put("MY_FOO_1_BAR", "myfoo1bar");
        properties.put("MY_FOO_10_BAR", "myfoo10bar");

        assertThat(properties).allSatisfy((key, value) -> {
            String normalized = nameNormalizer.normalize(key);
            assertThat(normalized).isEqualTo(value);
        });
    }
}
