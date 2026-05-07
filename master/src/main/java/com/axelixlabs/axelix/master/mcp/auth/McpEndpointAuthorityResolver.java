package com.axelixlabs.axelix.master.mcp.auth;

import java.util.Optional;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.service.AuthorityResolver;
import com.axelixlabs.axelix.master.mcp.McpEndpoint;

/**
 * Component that is capable to resolve the {@link Authority} that is required to access the {@link McpEndpoint}.
 *
 * @see AuthorityResolver
 * @author Mikhail Polivakha
 */
public interface McpEndpointAuthorityResolver {

    /**
     * Resolve the authority that is required to gain access for the provided {@link McpEndpoint}.
     *
     * @param endpoint the endpoint to resolve hte authority for.
     *
     * @return an {@link Optional} authority that is required to access the resource.
     *         Might be {@link Optional#empty()} in case no authority is required to
     *         access the resource.
     */
    Optional<Authority> resolve(McpEndpoint endpoint);
}
