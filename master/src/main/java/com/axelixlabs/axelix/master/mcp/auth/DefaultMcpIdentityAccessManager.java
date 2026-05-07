package com.axelixlabs.axelix.master.mcp.auth;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.axelixlabs.axelix.common.auth.core.AuthenticationScheme;
import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.AuthorizationRequest;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.exception.AuthorizationException;
import com.axelixlabs.axelix.common.auth.exception.JwtProcessingException;
import com.axelixlabs.axelix.common.auth.service.Authorizer;
import com.axelixlabs.axelix.common.utils.Assert;
import com.axelixlabs.axelix.master.mcp.McpEndpoint;
import com.axelixlabs.axelix.master.mcp.auth.handler.McpAuthenticationHandler;

import org.springframework.stereotype.Component;

/**
 * Default implementation of the {@link McpIdentityAccessManager}.
 *
 * @author Mikhail Polivakha
 */
@Component
public class DefaultMcpIdentityAccessManager implements McpIdentityAccessManager {

    private final Map<AuthenticationScheme, McpAuthenticationHandler> mcpAuthenticationHandlers;
    private final McpEndpointResolver mcpEndpointResolver;
    private final McpEndpointAuthorityResolver mcpEndpointAuthorityResolver;
    private final Authorizer authorizer;

    public DefaultMcpIdentityAccessManager(
        Authorizer authorizer,
        McpEndpointResolver mcpEndpointResolver,
        McpEndpointAuthorityResolver mcpEndpointAuthorityResolver,
        List<McpAuthenticationHandler> mcpAuthenticationHandlers
    ) {
        this.authorizer = authorizer;
        this.mcpEndpointResolver = mcpEndpointResolver;
        this.mcpEndpointAuthorityResolver = mcpEndpointAuthorityResolver;
        this.mcpAuthenticationHandlers = mcpAuthenticationHandlers
            .stream()
            .collect(Collectors.toMap(McpAuthenticationHandler::supportedAuthSchema, Function.identity()));
    }

    @Override
    public void verifyAccess(String jsonRpcRequest, AuthorizationHeader authorizationHeader) throws AuthorizationException, JwtProcessingException {
        McpAuthenticationHandler mcpAuthenticationHandler = mcpAuthenticationHandlers.get(authorizationHeader.authSchema());

        Assert.notNull(mcpAuthenticationHandler, "Unable to find McpAuthenticationHandler to handle the authentication for this request, please report this to maintainers");

        @SuppressWarnings("NullAway") // null away does not recognize custom not null assertion
        User authenticatedUser = mcpAuthenticationHandler.handleAuthentication(authorizationHeader.credential());

        Optional<McpEndpoint> mcpEndpoint = mcpEndpointResolver.resolve(jsonRpcRequest);

        mcpEndpoint.ifPresent(endpoint -> authorizeAccess(endpoint, authenticatedUser));
    }

    private void authorizeAccess(McpEndpoint endpoint, User authenticatedUser) {
        Optional<Authority> resolvedAuthority = mcpEndpointAuthorityResolver.resolve(endpoint);

        resolvedAuthority.ifPresent(authority ->
            authorizer.authorize(authenticatedUser, new AuthorizationRequest(Set.of(authority))));
    }
}
