package com.axelixlabs.axelix.master.filter.auth;

import com.axelixlabs.axelix.common.auth.core.AuthenticationScheme;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;

public interface McpAuthenticationHandler {

    /**
     * Handle the MCP Authentication. Normal execution mean that
     *
     * @param credential The whatever value of the {@link HttpHeaders#AUTHORIZATION} header.
     *                   The Authentication Schema is supposed to be stripped out.
     * @param response   Http Response
     */
    void handleAuthentication(String credential, HttpServletResponse response);

    /**
     * @return the {@link AuthenticationScheme} supported by this {@link McpAuthenticationHandler}.
     */
    AuthenticationScheme supportedAuthSchema();
}
