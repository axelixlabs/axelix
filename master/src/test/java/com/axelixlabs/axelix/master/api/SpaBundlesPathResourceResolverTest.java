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
package com.axelixlabs.axelix.master.api;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link SpaBundlesPathResourceResolver}.
 *
 * @author Mikhail Polivakha
 */
class SpaBundlesPathResourceResolverTest {

    private static final String SPA_DIST = "spa-dist";

    private SpaBundlesPathResourceResolver resolver;
    private Resource location;
    private Resource indexHtml;

    @BeforeEach
    void setUp() {
        location = new ClassPathResource(SPA_DIST + "/");
        indexHtml = new ClassPathResource(SPA_DIST + "/index.html");
        resolver = new SpaBundlesPathResourceResolver(indexHtml);
    }

    @Test
    void resolveExistingAsset_returnsThatResource() throws IOException {
        // when.
        Resource resolved = resolver.getResource("assets/main.js", location);

        // then.
        assertNotNull(resolved);
        assertTrue(resolved.exists());
        assertTrue(resolved.isReadable());
        assertEquals("main.js", resolved.getFilename());
    }

    @Test
    void resolveExistingCss_returnsThatResource() throws IOException {
        // when.
        Resource resolved = resolver.getResource("assets/style.css", location);

        // then.
        assertNotNull(resolved);
        assertTrue(resolved.exists());
        assertEquals("style.css", resolved.getFilename());
    }

    @ParameterizedTest
    @ValueSource(strings = {"index.html", "nonexistent/path", "dashboard/settings"})
    void shouldResolveIndexHtml(String requestedResource) throws IOException {
        // when.
        Resource resolved = resolver.getResource(requestedResource, location);

        // then.
        assertArrayEquals(resolved.getContentAsByteArray(), indexHtml.getContentAsByteArray());
    }
}
