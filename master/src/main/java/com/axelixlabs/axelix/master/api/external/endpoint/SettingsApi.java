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
package com.axelixlabs.axelix.master.api.external.endpoint;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;

import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.response.settings.AuthenticationOption;
import com.axelixlabs.axelix.master.api.external.response.settings.AxelixSettings;
import com.axelixlabs.axelix.master.autoconfiguration.mcp.McpAutoConfiguration;

/**
 * API for retrieving master settings.
 *
 * @since 06.03.2026
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@ExternalApiRestController
public class SettingsApi {

    private final List<AuthenticationOption> authSettings;
    private final boolean isMcpServerEnabled;

    public SettingsApi(
            @Value("${" + McpAutoConfiguration.MCP_CONFIGURATION_PROPERTIES_PREFIX + ".enabled" + "}")
                    boolean isMcpServerEnabled,
            List<AuthenticationOption> authSettings) {
        this.isMcpServerEnabled = isMcpServerEnabled;
        this.authSettings = authSettings;
    }

    @GetMapping(path = ApiPaths.SettingsApi.SETTINGS)
    public AxelixSettings getAxelixSettings() {
        return new AxelixSettings(authSettings, isMcpServerEnabled);
    }
}
