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

import static com.axelixlabs.axelix.e2e.utils.UserUtils.PASSWORD;
import static com.axelixlabs.axelix.e2e.utils.UserUtils.generateUniqueEmail;
import static com.axelixlabs.axelix.e2e.utils.UserUtils.generateUniqueUsername;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * E2E tests validating the local user authentication flow and user management actions.
 *
 * @author Nikita Kirillov
 */
public class LocalAuthE2ETest {

    private AxelixMasterApiClient client;

    @BeforeEach
    void setUp() {
        assumeTrue(
                E2ETestConfig.authModeLocalEnabled(), "Auth Local mode is disabled in configuration, skipping test.");

        client = new AxelixMasterApiClient(E2ETestConfig.masterBaseUrl());
        client.login(E2ETestConfig.superAdminUsername(), E2ETestConfig.superAdminPassword());
    }

    @Test
    void shouldLoginLocalUser() {
        // given when.
        String username = generateUniqueUsername();
        client.registerLocalUser(username, generateUniqueEmail(), PASSWORD, "EDITOR");

        // logout as super-admin
        client.logout();

        // then.
        client.login(username, PASSWORD);
    }
}
