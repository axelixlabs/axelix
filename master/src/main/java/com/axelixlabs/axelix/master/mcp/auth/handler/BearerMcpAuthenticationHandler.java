package com.axelixlabs.axelix.master.mcp.auth.handler;

import java.util.Set;

import com.axelixlabs.axelix.common.auth.core.AuthenticationScheme;
import com.axelixlabs.axelix.common.auth.core.AuthenticationSchemes;
import com.axelixlabs.axelix.common.auth.core.PasswordlessUser;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.exception.auth.AuthenticationException;
import com.axelixlabs.axelix.master.exception.auth.OidcTokenExchangeException;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcClient;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcRoleExtractor;

/**
 * {@link McpAuthenticationHandler} that is capable to authenticate {@link AuthenticationSchemes#BEARER Bearer auth} requests.
 *
 * @author Mikhail Polivakha
 */
public class BearerMcpAuthenticationHandler implements McpAuthenticationHandler {

    private final OidcRoleExtractor roleExtractor;

    public BearerMcpAuthenticationHandler(OidcRoleExtractor roleExtractor) {
        this.roleExtractor = roleExtractor;
    }

    @Override
    public User handleAuthentication(String credential) throws AuthenticationException {
        // credential is expected to be an access token
        try {
            Role role = roleExtractor.extractRole(credential);
            return new PasswordlessUser("MCP_AGENT", Set.of(role));
        } catch (OidcTokenExchangeException e) {
            throw new AuthenticationException();
        }
    }

    @Override
    public AuthenticationScheme supportedAuthSchema() {
        return AuthenticationSchemes.BEARER;
    }
}
