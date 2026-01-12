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
package com.nucleonforge.axelix.master.utils;

import java.util.function.Function;

import org.springframework.boot.test.web.client.TestRestTemplate;

import com.nucleonforge.axelix.master.TestRestTemplateBuilder;

/**
 * This enum contains a set of invalid authentication scenarios used to parameterize API integration tests.
 *
 * @author Sergey Cherkasov
 */
public enum InvalidAuthScenario {

    // Request with invalid cookie-based authentication.
    INVALID_COOKIE(TestRestTemplateBuilder::withInvalidCookie),

    // Request without an authentication token.
    NO_TOKEN(TestRestTemplateBuilder::withoutToken),

    // Request with an expired authentication token.
    EXPIRED_TOKEN(TestRestTemplateBuilder::withExpiredToken);

    /**
     * Modifier function that applies an invalid authentication scenario
     * to {@link TestRestTemplateBuilder} and return {@link TestRestTemplate}.
     */
    public final Function<TestRestTemplateBuilder, TestRestTemplate> modifier;

    InvalidAuthScenario(Function<TestRestTemplateBuilder, TestRestTemplate> modifier) {
        this.modifier = modifier;
    }
}
