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
package com.axelixlabs.axelix.e2e.config;

/**
 * Configuration registry providing access to environment properties
 * and system settings for Axelix Master E2E tests.
 *
 * @author Nikita Kirillov
 */
public final class E2ETestConfig {

    private E2ETestConfig() {}

    public static String masterBaseUrl() {
        return System.getProperty("axelix.e2e.masterBaseUrl");
    }

    public static String superAdminUsername() {
        return System.getProperty("axelix.e2e.superAdmin.username");
    }

    public static String superAdminPassword() {
        return System.getProperty("axelix.e2e.superAdmin.password");
    }

    public static boolean isAutoDiscoveryMode() {
        return System.getProperty("axelix.e2e.discoveryModeAutoEnabled").equalsIgnoreCase("true");
    }

    public static boolean isDiscoveryModeSelfReg() {
        return System.getProperty("axelix.e2e.discoveryModeSelfRegEnabled").equalsIgnoreCase("true");
    }

    public static boolean authModeLocalEnabled() {
        return System.getProperty("axelix.e2e.authModeLocalEnabled").equalsIgnoreCase("true");
    }

    public static boolean authModeOAuth2Enabled() {
        return System.getProperty("axelix.e2e.authModeOAuth2Enabled").equalsIgnoreCase("true");
    }

    public static boolean isMcpEnabled() {
        return System.getProperty("axelix.e2e.mcpEnabled").equalsIgnoreCase("true");
    }

    public static String oAuth2TestUsername() {
        return System.getProperty("axelix.e2e.oidc.username", "user");
    }

    public static String oAuth2TestPassword() {
        return System.getProperty("axelix.e2e.oidc.password", "user");
    }
}
