package com.axelixlabs.axelix.master.mcp.auth;

import com.axelixlabs.axelix.common.auth.exception.AuthorizationException;
import com.axelixlabs.axelix.common.auth.exception.JwtProcessingException;
import com.axelixlabs.axelix.common.auth.service.WebIdentityAccessManager;

/**
 * The main entrypoint for evaluating the possibility of processing requests came from the AI Agent (both Authentication
 * and Authorization). So essentially this service is the entrypoint for IAM checks for all requests made by AI Agents to
 * Axelix Master MCP.
 *
 * @see WebIdentityAccessManager Similar abstraction but for handling web requests.
 *
 * @author Mikhail Polivakha
 */
public interface McpIdentityAccessManager {

    /**
     * Main entrypoint for MCP requests IAM. In case any problem is encountered, then the corresponding exception is thrown.
     * In case access is granted, the method returns the user identified by the bearer access token has been granted access.
     * <p>
     * Please note that the user that is returned by this call, is the user that is using the AI Agent, i.e. it is not the user
     * that represents the AI Agent itself.
     *
     * @param jsonRpcRequest      the body of the http request (in case of mcp client, this is a JSON-RPC request).
     * @param authorizationHeader the contents of the incoming http authorization header.
     * @throws AuthorizationException in case the user is not authorized to access the given API.
     * @throws JwtProcessingException in case the implementation is unable to verify the validity
     *                                of the token or if the token is deemed invalid.
     */
    void verifyAccess(String jsonRpcRequest, AuthorizationHeader authorizationHeader)
        throws AuthorizationException, JwtProcessingException;
}
