package com.axelixlabs.axelix.master.filter.auth;

import java.util.Base64;
import java.util.Base64.Decoder;

import com.axelixlabs.axelix.common.auth.core.AuthenticationScheme;
import com.axelixlabs.axelix.common.auth.core.AuthenticationSchemes;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.exception.auth.McpAuthenticationException;
import com.axelixlabs.axelix.master.service.auth.provider.UserAuthenticator;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter that authenticates requests to MCP endpoints using either OAuth2 Bearer tokens
 * or Basic Authentication.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public class McpBasicAuthFilter implements McpAuthenticationHandler {

    private static final Logger log = LoggerFactory.getLogger(McpBasicAuthFilter.class);
    private final UserAuthenticator userProvider;

    private final Decoder decoder;

    public McpBasicAuthFilter(UserAuthenticator userProvider) {
        this.userProvider = userProvider;
        this.decoder = Base64.getDecoder();
    }

    @Override
    public void handleAuthentication(String credential, HttpServletResponse response) {
        try {
            String[] parts = new String(decoder.decode(credential)).split(":", 2);

            if (parts.length != 2) {
                throw new McpAuthenticationException("Invalid basic auth format");
            }

            String login = parts[0];
            String password = parts[1];

            User authenticatedUser = userProvider.authenticate(login, password);

            // TODO: Introduce permissions check here
        } catch (Exception e) {
            log.debug("Basic auth validation failed: {}", e.getMessage());
        }
    }

    @Override
    public AuthenticationScheme supportedAuthSchema() {
        return AuthenticationSchemes.BASIC;
    }

}
