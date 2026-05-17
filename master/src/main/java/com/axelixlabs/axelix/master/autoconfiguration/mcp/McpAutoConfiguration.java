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
package com.axelixlabs.axelix.master.autoconfiguration.mcp;

import java.util.List;

import tools.jackson.databind.json.JsonMapper;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.auth.service.Authorizer;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.OAuth2Properties;
import com.axelixlabs.axelix.master.filter.auth.McpAuthorizationFilter;
import com.axelixlabs.axelix.master.mcp.auth.DefaultMcpEndpointAuthorityResolver;
import com.axelixlabs.axelix.master.mcp.auth.DefaultMcpEndpointResolver;
import com.axelixlabs.axelix.master.mcp.auth.DefaultMcpIdentityAccessManager;
import com.axelixlabs.axelix.master.mcp.auth.McpEndpointAuthorityResolver;
import com.axelixlabs.axelix.master.mcp.auth.McpEndpointResolver;
import com.axelixlabs.axelix.master.mcp.auth.McpIdentityAccessManager;
import com.axelixlabs.axelix.master.mcp.auth.handler.McpAuthenticationHandler;

/**
 * Auto-Configuration for the MCP-related components
 *
 * @author Mikhail Polivakha
 */
@AutoConfiguration
@ConditionalOnMcpServerEnabled
public class McpAutoConfiguration {

    public static final String MCP_CONFIGURATION_PROPERTIES_PREFIX = "axelix.master.mcp-server";

    @Bean
    public McpAuthorizationFilter mcpAuthenticationFilter(
            ObjectProvider<OAuth2Properties> oAuth2PropertiesObjectProvider,
            McpIdentityAccessManager mcpIdentityAccessManager,
            SecurityContextExecutor securityContextExecutor,
            JwtEncoderService jwtEncoderService) {
        return new McpAuthorizationFilter(
                oAuth2PropertiesObjectProvider, mcpIdentityAccessManager, securityContextExecutor, jwtEncoderService);
    }

    @Bean
    public McpIdentityAccessManager mcpIdentityAccessManager(
            Authorizer authorizer,
            McpEndpointResolver mcpEndpointResolver,
            McpEndpointAuthorityResolver mcpEndpointAuthorityResolver,
            List<McpAuthenticationHandler> mcpAuthenticationHandlers) {
        return new DefaultMcpIdentityAccessManager(
                authorizer, mcpEndpointResolver, mcpEndpointAuthorityResolver, mcpAuthenticationHandlers);
    }

    @Bean
    public McpEndpointResolver mcpEndpointResolver() {
        return new DefaultMcpEndpointResolver(new JsonMapper());
    }

    @Bean
    public McpEndpointAuthorityResolver mcpEndpointAuthorityResolver() {
        return new DefaultMcpEndpointAuthorityResolver();
    }
}
