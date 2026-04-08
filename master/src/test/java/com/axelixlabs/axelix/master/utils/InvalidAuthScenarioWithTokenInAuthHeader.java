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
package com.axelixlabs.axelix.master.utils;

import java.util.function.Function;

import org.springframework.boot.resttestclient.TestRestTemplate;

/**
 * This enum contains a set of invalid authentication scenarios with a token in the Authentication Header,
 * used to parameterize API self-registration integration tests.
 *
 * @author Nikita Kirillov
 */
public enum InvalidAuthScenarioWithTokenInAuthHeader {

    // Request without a token in authorization header.
    WITHOUT_TOKEN(TestRestTemplateBuilder::withoutToken),

    // Request with an expired authentication token.
    EXPIRED_TOKEN(TestRestTemplateBuilder::withExpiredTokenInAuthHeader),

    // Request with a malformed authentication token.
    MALFORMED_TOKEN(TestRestTemplateBuilder::withMalformedTokenInAuthHeader);

    /**
     * Modifier function that applies an invalid authentication scenario
     * to {@link TestRestTemplateBuilder} and returns {@link TestRestTemplate}.
     */
    private final Function<TestRestTemplateBuilder, TestRestTemplate> modifier;

    InvalidAuthScenarioWithTokenInAuthHeader(Function<TestRestTemplateBuilder, TestRestTemplate> modifier) {
        this.modifier = modifier;
    }

    public Function<TestRestTemplateBuilder, TestRestTemplate> getModifier() {
        return modifier;
    }
}
