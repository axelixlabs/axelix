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
package com.axelixlabs.axelix.master.mcp.auth.handler;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelixlabs.axelix.common.auth.core.AuthenticationScheme;
import com.axelixlabs.axelix.common.auth.core.AuthenticationSchemes;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.exception.auth.AuthenticationException;
import com.axelixlabs.axelix.master.service.auth.provider.UserAuthenticator;

/**
 * {@link McpAuthenticationHandler} that is capable to authenticate {@link AuthenticationSchemes#BASIC Basic auth} requests.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public class BasicMcpAuthenticationHandler implements McpAuthenticationHandler {

    private static final Logger log = LoggerFactory.getLogger(BasicMcpAuthenticationHandler.class);

    private final UserAuthenticator userAuthenticator;
    private final Decoder decoder;

    public BasicMcpAuthenticationHandler(UserAuthenticator userAuthenticator) {
        this.userAuthenticator = userAuthenticator;
        this.decoder = Base64.getDecoder();
    }

    @Override
    public User handleAuthentication(String credential) {
        try {
            String[] parts = new String(decoder.decode(credential), StandardCharsets.UTF_8).split(":", 2);

            if (parts.length != 2) {
                throw new AuthenticationException("Invalid basic auth format");
            }

            String login = parts[0];
            String password = parts[1];

            return Optional.ofNullable(userAuthenticator.authenticate(login, password))
                    .orElseThrow(AuthenticationException::new);

        } catch (Exception e) {
            log.warn("Basic authentication for accessing the MCP failed: {}", e.getMessage());
            throw new AuthenticationException(e);
        }
    }

    @Override
    public AuthenticationScheme supportedAuthScheme() {
        return AuthenticationSchemes.BASIC;
    }
}
