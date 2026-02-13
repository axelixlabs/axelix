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
package com.axelixlabs.axelix.master.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;

import com.axelixlabs.axelix.master.ApplicationEntrypoint;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests that static resources from the SPA dist are served correctly,
 * including SPA fallback to index.html for client routes.
 *
 * @author Mikhail Polivakha
 */
@SpringBootTest(classes = ApplicationEntrypoint.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpaStaticResourcesServingFilterTest {

    private static final String SPA_DIST = "spa-dist";

    @Autowired
    private TestRestTemplateBuilder restTemplateBuilder;

    @Test
    void servesExistingAsset() throws Exception {
        String expected =
                StreamUtils.copyToString(new ClassPathResource(SPA_DIST + "/assets/main.js").getInputStream(), UTF_8);

        TestRestTemplate rest = restTemplateBuilder.withoutAuthorities();
        ResponseEntity<String> response = rest.getForEntity("/assets/main.js", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("text/javascript"));
    }

    @Test
    void servesExistingCss() throws Exception {
        String expected =
                StreamUtils.copyToString(new ClassPathResource(SPA_DIST + "/assets/style.css").getInputStream(), UTF_8);

        TestRestTemplate rest = restTemplateBuilder.withoutAuthorities();
        ResponseEntity<String> response = rest.getForEntity("/assets/style.css", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("text/css"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "/index.html", "/wallboard"})
    void shouldServesIndexHtml(String contextPath) throws Exception {
        String expected =
                StreamUtils.copyToString(new ClassPathResource(SPA_DIST + "/index.html").getInputStream(), UTF_8);

        TestRestTemplate rest = restTemplateBuilder.withoutAuthorities();
        ResponseEntity<String> response = rest.getForEntity(contextPath, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.parseMediaType("text/html"));
    }
}
