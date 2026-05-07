package com.axelixlabs.axelix.master.mcp.auth.handler;

import com.axelixlabs.axelix.common.auth.core.AuthenticationScheme;
import com.axelixlabs.axelix.common.auth.core.AuthenticationSchemes;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.exception.auth.AuthenticationException;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcClient;

/**
 * {@link McpAuthenticationHandler} that is capable to authenticate {@link AuthenticationSchemes#BEARER Bearer auth} requests.
 *
 * @author Mikhail Polivakha
 */
public class BearerMcpAuthenticationHandler implements McpAuthenticationHandler {

    private final OidcClient oidcClient;

    public BearerMcpAuthenticationHandler(OidcClient oidcClient) {
        this.oidcClient = oidcClient;
    }

    @Override
    public User handleAuthentication(String credential) throws AuthenticationException {
    }

    @Override
    public AuthenticationScheme supportedAuthSchema() {
        return AuthenticationSchemes.BEARER;
    }
}
