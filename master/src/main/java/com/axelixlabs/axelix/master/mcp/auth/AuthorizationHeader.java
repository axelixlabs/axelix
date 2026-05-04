package com.axelixlabs.axelix.master.mcp.auth;

import com.axelixlabs.axelix.common.auth.core.AuthenticationScheme;

import org.springframework.http.HttpHeaders;

/**
 * Contents of the {@link HttpHeaders#AUTHORIZATION} header.
 *
 * @author Mikhail Polivakha
 */
public record AuthorizationHeader(AuthenticationScheme authSchema, String credential) {}
