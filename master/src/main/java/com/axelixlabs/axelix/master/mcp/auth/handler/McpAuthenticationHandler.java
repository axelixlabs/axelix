package com.axelixlabs.axelix.master.mcp.auth.handler;

import com.axelixlabs.axelix.common.auth.core.AuthenticationScheme;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.exception.auth.AuthenticationException;

import org.springframework.http.HttpHeaders;

/**
 * Component that is capable to authenticate the user of the MCP Server given the provided credential.
 *
 * @author Mikhail Polivakha
 */
public interface McpAuthenticationHandler {

    /**
     * Handle the MCP Authentication.
     *
     * @param credential                The whatever value of the {@link HttpHeaders#AUTHORIZATION} header.
     *                                  The Authentication Schema is supposed to be stripped out.
     *
     * @return                          The authenticated {@link User}.
     * @throws AuthenticationException  In case of the authentication failure.
     */
    User handleAuthentication(String credential) throws AuthenticationException;

    /**
     * @return the {@link AuthenticationScheme} supported by this {@link McpAuthenticationHandler}.
     */
    AuthenticationScheme supportedAuthSchema();
}
