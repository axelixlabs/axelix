package com.axelixlabs.axelix.master.mcp.auth.handler;

import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Optional;

import com.axelixlabs.axelix.common.auth.core.AuthenticationScheme;
import com.axelixlabs.axelix.common.auth.core.AuthenticationSchemes;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.exception.auth.AuthenticationException;
import com.axelixlabs.axelix.master.exception.auth.McpAuthenticationException;
import com.axelixlabs.axelix.master.service.auth.provider.UserAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link McpAuthenticationHandler} that is capable to authenticate {@link AuthenticationSchemes#BASIC Basic auth} requests.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public class BasicMcpAuthenticationHandler implements McpAuthenticationHandler {

    private static final Logger log = LoggerFactory.getLogger(BasicMcpAuthenticationHandler.class);

    private final UserAuthenticator userAuthenticator;
    private final Decoder decoder;

    public BasicMcpAuthenticationHandler(UserAuthenticator userAuthenticator) {
        this.userAuthenticator = userAuthenticator;
        this.decoder = Base64.getDecoder();
    }

    @Override
    public User handleAuthentication(String credential) {
        try {
            String[] parts = new String(decoder.decode(credential)).split(":", 2);

            if (parts.length != 2) {
                throw new McpAuthenticationException("Invalid basic auth format");
            }

            String login = parts[0];
            String password = parts[1];

            return Optional
                .ofNullable(userAuthenticator.authenticate(login, password))
                .orElseThrow(AuthenticationException::new);

        } catch (Exception e) {
            log.debug("Basic authentication for accessing the MCP failed: {}", e.getMessage());
            throw new AuthenticationException();
        }
    }

    @Override
    public AuthenticationScheme supportedAuthSchema() {
        return AuthenticationSchemes.BASIC;
    }

}
