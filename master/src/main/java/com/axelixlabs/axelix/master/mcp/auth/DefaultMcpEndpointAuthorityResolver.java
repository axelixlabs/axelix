package com.axelixlabs.axelix.master.mcp.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.master.mcp.McpEndpoint;
import com.axelixlabs.axelix.master.mcp.McpEndpoints;

import org.springframework.stereotype.Component;

/**
 * Default implementation of {@link McpEndpointAuthorityResolver} that hosts the mapping in-memory.
 *
 * @author Mikhail Polivakha
 */
@Component
public class DefaultMcpEndpointAuthorityResolver implements McpEndpointAuthorityResolver {

    private static final Map<McpEndpoint, Authority> MAPPING;

    static {
        MAPPING = new HashMap<>(2);
        MAPPING.put(McpEndpoints.CLEAR_ALL_CACHES, DefaultAuthority.CACHES_CLEAR);
        MAPPING.put(McpEndpoints.CLEAR_SPECIFIC_CACHE, DefaultAuthority.CACHES_CLEAR);
    }

    @Override
    public Optional<Authority> resolve(McpEndpoint endpoint) {
        return Optional.ofNullable(MAPPING.get(endpoint));
    }
}
