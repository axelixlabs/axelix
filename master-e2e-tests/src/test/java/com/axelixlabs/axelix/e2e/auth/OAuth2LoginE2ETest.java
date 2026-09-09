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
package com.axelixlabs.axelix.e2e.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.e2e.client.AxelixMasterApiClient;
import com.axelixlabs.axelix.e2e.config.E2ETestConfig;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * E2E tests validating the OAuth2/OIDC user authentication flow.
 *
 * @author Nikita Kirillov
 */
public class OAuth2LoginE2ETest {

    private AxelixMasterApiClient client;

    @BeforeEach
    void setUp() {
        assumeTrue(E2ETestConfig.authModeOAuth2Enabled(), "OAuth2/OIDC auth is disabled for this configuration.");
        client = new AxelixMasterApiClient(E2ETestConfig.masterBaseUrl());
    }

    @Test
    void shouldLoginViaOAuth2Provider() {
        // given when.
        client.loginViaOAuth2(E2ETestConfig.oAuth2TestUsername(), E2ETestConfig.oAuth2TestPassword());

        // then.
        // If the OIDC round-trip actually set a valid auth cookie, an authenticated call
        // through the same client should now succeed.
        client.getRegisteredInstanceNames();
    }
}
