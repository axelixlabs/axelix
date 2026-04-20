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
import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.master.exception.auth.OidcTokenExchangeException;
import com.axelixlabs.axelix.master.service.auth.oauth.DefaultOidcClient.Tokens;

/**
 * Extracts user role from OIDC tokens.
 *
 * @author Nikita Kirillov
 */
public class OidcRoleExtractor {

    private static final Logger log = LoggerFactory.getLogger(OidcRoleExtractor.class);

    private final Decoder decoder = Base64.getUrlDecoder();
    private final OidcClient oidcClient;

    @Nullable
    private final String roleAttributePath;

    private final Map<String, List<String>> roleMapping;

    public OidcRoleExtractor(
            OidcClient oidcClient, @Nullable String roleAttributePath, Map<String, List<String>> roleMapping) {
        this.oidcClient = oidcClient;
        this.roleAttributePath = roleAttributePath;
        this.roleMapping = roleMapping;
    }

    /**
     * First we try to extract the role from the idToken,
     * then we try to extract the role from the userInfo endpoint.
     *
     * @return extracted role, or DefaultRole.VIEWER (fallback).
     */
    public Role extractRole(Tokens tokens) {
        if (roleAttributePath == null || roleAttributePath.isBlank()) {
            return DefaultRole.VIEWER;
        }

        Role role = extractRoleFromIdToken(tokens.idToken());

        if (role == null) {
            role = extractRoleFromUserInfo(tokens.accessToken());
        }

        return role != null ? role : DefaultRole.VIEWER;
    }

    @Nullable
    private Role extractRoleFromIdToken(String idToken) {
        try {
            String json = decodeIdToken(idToken);
            return extractRoleFromJson(json);
        } catch (Exception e) {
            log.debug("Failed to extract role from ID token: {}", e.getMessage());
        }
        return null;
    }

    @Nullable
    private Role extractRoleFromUserInfo(String accessToken) {
        try {
            String userInfo = oidcClient.validateAccessTokenAndExtractUserInfo(accessToken);
            if (userInfo == null) {
                return null;
            }
            return extractRoleFromJson(userInfo);
        } catch (OidcTokenExchangeException e) {
            log.debug("Failed to extract role from UserInfo: {}", e.getMessage());
        }
        return null;
    }

    @Nullable
    private Role extractRoleFromJson(String json) {
        try {
            Object readData = JsonPath.read(json, roleAttributePath);
            List<String> externalRoles = (readData instanceof List<?> list)
                    ? list.stream().map(Object::toString).toList()
                    : List.of(readData.toString());

            for (String externalRole : externalRoles) {
                String targetName = roleMapping.entrySet().stream()
                        .filter(entry ->
                                entry.getValue().stream().anyMatch(value -> value.equalsIgnoreCase(externalRole)))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(externalRole);

                Role role = stringToRole(targetName);

                if (role != null) {
                    return role;
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract role from JSON: {}", e.getMessage());
        }
        return null;
    }

    private String decodeIdToken(String idToken) {
        String[] parts = idToken.split("\\.");
        byte[] decoded = decoder.decode(parts[1]);
        return new String(decoded);
    }

    @Nullable
    private Role stringToRole(String name) {
        return switch (name.toLowerCase()) {
            case "admin" -> DefaultRole.ADMIN;
            case "editor" -> DefaultRole.EDITOR;
            default -> null;
        };
    }
}
