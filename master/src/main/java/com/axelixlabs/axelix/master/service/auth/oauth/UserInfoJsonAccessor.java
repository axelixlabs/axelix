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
package com.axelixlabs.axelix.master.service.auth.oauth;

import java.util.Locale;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.OAuth2Properties;
import com.axelixlabs.axelix.master.exception.auth.OidcRoleExtractionException;
import com.axelixlabs.axelix.master.service.state.RoleService;

/**
 * Accessor for the JSON response of the {@code /userinfo} OIDC endpoint.
 *
 * @author Mikhail Polivakha
 */
public class UserInfoJsonAccessor {

    private static final String DEFAULT_ROLE_NAME = "VIEWER";

    private final JmesPathJsonInspector jsonInspector;
    private final OAuth2Properties oAuth2Properties;
    private final RoleService roleService;

    public UserInfoJsonAccessor(
            JmesPathJsonInspector jsonInspector, OAuth2Properties oAuth2Properties, RoleService roleService) {
        this.jsonInspector = jsonInspector;
        this.oAuth2Properties = oAuth2Properties;
        this.roleService = roleService;
    }

    @Nullable
    public String extractTextField(String userInfoJson, String field) {
        return jsonInspector.extract(userInfoJson, field);
    }

    public Role extractRole(String userInfoJson) {
        String roleAttributePath = oAuth2Properties.roleAttributePath();
        if (roleAttributePath == null || roleAttributePath.isBlank()) {
            return resolveRole(DEFAULT_ROLE_NAME, roleAttributePath);
        }

        String roleName = jsonInspector.extract(userInfoJson, roleAttributePath);
        if (roleName == null) {
            throw new OidcRoleExtractionException(String.format(
                    "Failed to extract role from UserInfo JSON payload using JMESPath expression: '%s'",
                    roleAttributePath));
        }
        return resolveRole(roleName, roleAttributePath);
    }

    private Role resolveRole(String roleName, @Nullable String roleAttributePath) {
        return roleService
                .findByName(roleName.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new OidcRoleExtractionException(String.format(
                        "Failed to extract role from UserInfo JSON payload using JMESPath expression: '%s'",
                        roleAttributePath)));
    }
}
