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
package com.axelixlabs.axelix.master.api.infrastructure;

import java.util.List;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;

import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.OAuth2Properties;
import com.axelixlabs.axelix.master.autoconfiguration.web.WebAutoConfiguration;

/**
 * Exposes OAuth2 Protected Resource Metadata for MCP authentication discovery.
 *
 * @author Nikita Kirillov
 */
@ExternalApiRestController
@ConditionalOnProperty(prefix = "axelix.master.auth.options.oauth2", name = "enabled", havingValue = "true")
public class McpOAuth2MetadataController {

    private final String issuerUri;
    private final String mcpServerFullPath;
    private final String scopes;

    public McpOAuth2MetadataController(OAuth2Properties oAuth2Properties) {
        this.mcpServerFullPath = oAuth2Properties.baseUrl() + WebAutoConfiguration.EXTERNAL_API_PATH + "/api/mcp";
        this.issuerUri = oAuth2Properties.issuerUri();
        this.scopes = oAuth2Properties.scopes();
    }

    @GetMapping(ApiPaths.McpOAuth2Api.PROTECTED_RESOURCE_METADATA)
    public Map<String, Object> protectedResourceMetadata() {
        return Map.of(
                "resource", mcpServerFullPath,
                "authorization_servers", List.of(issuerUri),
                "scopes_supported", List.of(scopes));
    }
}
