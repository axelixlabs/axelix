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

import java.util.Base64;
import java.util.Base64.Decoder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.burt.jmespath.Expression;
import io.burt.jmespath.jackson.JacksonRuntime;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.master.exception.auth.OidcMetadataUnavailableException;
import com.axelixlabs.axelix.master.exception.auth.OidcTokenExchangeException;

/**
 * Extracts user role from OIDC tokens using a JMESPath expression.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public class JmesPathOidcRoleExtractor implements OidcRoleExtractor {

    private static final Logger log = LoggerFactory.getLogger(JmesPathOidcRoleExtractor.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Decoder decoder = Base64.getUrlDecoder();

    @Nullable
    private final Expression<JsonNode> jmesPathExpression;

    private final OidcClient oidcClient;

    public JmesPathOidcRoleExtractor(OidcClient oidcClient, @Nullable String roleAttributePath) {
        this.oidcClient = oidcClient;

        if (roleAttributePath != null && !roleAttributePath.isEmpty()) {
            this.jmesPathExpression = new JacksonRuntime().compile(roleAttributePath);
        } else {
            this.jmesPathExpression = null;
        }
    }

    @Override
    public Role extractRole(String accessToken) throws OidcTokenExchangeException {
        if (jmesPathExpression == null) {
            return DefaultRole.VIEWER;
        }

        Role role = extractRoleFromUserInfo(accessToken);

        if (role == null) {
            throw new OidcTokenExchangeException(String.format(
                    "Failed to extract role from tokens. JMES path expression: '%s' - role not found in ID token nor UserInfo endpoint",
                    jmesPathExpression));
        }

        return role;
    }

    @Nullable
    public Role extractRoleFromUserInfo(String accessToken) {
        try {
            String userInfo = oidcClient.validateAccessTokenAndExtractUserInfo(accessToken);

            if (userInfo == null) {
                return null;
            }

            return extractRoleFromJson(userInfo);
        } catch (OidcTokenExchangeException | OidcMetadataUnavailableException e) {
            log.debug("Failed to extract role from UserInfo: {}", e.getMessage());
        }

        return null;
    }

    @Nullable
    private Role extractRoleFromJson(String json) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(json);
            @SuppressWarnings("NullAway")
            JsonNode evaluatedResult = jmesPathExpression.search(root);

            if (evaluatedResult == null || evaluatedResult.isNull() || !evaluatedResult.isTextual()) {
                return null;
            }

            return stringToRole(evaluatedResult.asText());
        } catch (Exception e) {
            log.warn("Failed to extract role from JSON: {}", e.getMessage());
        }
        return null;
    }

    @Nullable
    private Role stringToRole(String name) {
        return switch (name.toLowerCase()) {
            case "admin" -> DefaultRole.ADMIN;
            case "editor" -> DefaultRole.EDITOR;
            case "viewer" -> DefaultRole.VIEWER;
            default -> null;
        };
    }
}
