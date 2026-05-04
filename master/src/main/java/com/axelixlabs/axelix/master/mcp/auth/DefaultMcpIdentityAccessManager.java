package com.axelixlabs.axelix.master.mcp.auth;

import java.util.Map;

import com.axelixlabs.axelix.common.auth.core.AuthenticationScheme;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.exception.AuthorizationException;
import com.axelixlabs.axelix.common.auth.exception.JwtProcessingException;
import com.axelixlabs.axelix.common.utils.Assert;
import com.axelixlabs.axelix.master.filter.auth.McpAuthenticationHandler;
import com.axelixlabs.axelix.master.service.auth.oauth.OidcClient;
import org.jspecify.annotations.Nullable;

public class DefaultMcpIdentityAccessManager implements McpIdentityAccessManager {

    private final OidcClient oidcClient;
    private final Map<AuthenticationScheme, McpAuthenticationHandler> mcpAuthenticationHandlers;

    public DefaultMcpIdentityAccessManager(OidcClient oidcClient) {
        this.oidcClient = oidcClient;
        mcpAuthenticationHandlers = Map.of();
    }

    @Override
    public User verifyAccess(String jsonRpcRequest, AuthorizationHeader authorizationHeader) throws AuthorizationException, JwtProcessingException {
        McpAuthenticationHandler mcpAuthenticationHandler = mcpAuthenticationHandlers.get(authorizationHeader.authSchema());

        Assert.notNull(mcpAuthenticationHandler, "Unable to find McpAuthenticationHandler to handle the authentication for this request, please report this to maintainers");

        mcpAuthenticationHandler.handleAuthentication(authorizationHeader.credential(), null);
    }
}
