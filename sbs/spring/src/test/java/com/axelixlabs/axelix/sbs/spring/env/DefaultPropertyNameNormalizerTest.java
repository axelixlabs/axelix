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
package com.axelixlabs.axelix.sbs.spring.env;

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
