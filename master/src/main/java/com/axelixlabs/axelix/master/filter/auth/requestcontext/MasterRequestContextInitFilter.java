/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.master.filter.auth.requestcontext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.Nullable;

import org.springframework.ai.mcp.server.common.autoconfigure.properties.McpServerStreamableHttpProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import com.axelixlabs.axelix.common.domain.http.HttpMethod;
import com.axelixlabs.axelix.master.autoconfiguration.web.WebAutoConfiguration;
import com.axelixlabs.axelix.master.filter.ContentCachingServletRequest;
import com.axelixlabs.axelix.master.filter.FiltersOrder;
import com.axelixlabs.axelix.master.mcp.McpEndpoint;
import com.axelixlabs.axelix.master.mcp.auth.McpEndpointResolver;
import com.axelixlabs.axelix.master.service.auth.MasterWebEndpoint;
import com.axelixlabs.axelix.master.service.auth.MasterWebEndpointResolver;

/**
 * {@link OncePerRequestFilter} whose job is to record the profile of the request that was attempted to
 * be executed.
 *
 * @author Mikhail Polivakha
 */
@Order(FiltersOrder.REQUEST_PROFILE_FILTER)
public class MasterRequestContextInitFilter extends OncePerRequestFilter {

    private static final ScopedValue<MasterRequestContext> MASTER_REQUEST_CONTEXT = ScopedValue.newInstance();

    // Mcp Components might not be available if mcp server is not enabled.
    private final @Nullable McpEndpointResolver mcpEndpointResolver;
    private final @Nullable McpServerStreamableHttpProperties mcpProperties;

    private final MasterWebEndpointResolver masterWebEndpointResolver;

    public MasterRequestContextInitFilter(
            ObjectProvider<McpEndpointResolver> mcpEndpointResolver,
            MasterWebEndpointResolver masterWebEndpointResolver,
            ObjectProvider<McpServerStreamableHttpProperties> mcpProperties) {
        this.mcpEndpointResolver = mcpEndpointResolver.getIfAvailable();
        this.mcpProperties = mcpProperties.getIfAvailable();
        this.masterWebEndpointResolver = masterWebEndpointResolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !shouldFilter(request);
    }

    private boolean shouldFilter(HttpServletRequest request) {
        String servletPath = request.getServletPath();

        return servletPath.startsWith(WebAutoConfiguration.EXTERNAL_API_PATH)
                || (mcpProperties != null && servletPath.startsWith(mcpProperties.getMcpEndpoint()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String servletPath = request.getServletPath();

        if (servletPath.startsWith(WebAutoConfiguration.EXTERNAL_API_PATH)) {
            processExternalWebRequest(request, response, filterChain, servletPath);
        }

        if (mcpProperties != null && servletPath.startsWith(mcpProperties.getMcpEndpoint())) {
            processExternalMcpRequest(request, response, filterChain);
        }
    }

    private void processExternalMcpRequest(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        if (mcpEndpointResolver != null) {
            var wrapper = new ContentCachingServletRequest(request);
            var requestAsString = new String(wrapper.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            // Requests that do not target a concrete tool endpoint (e.g. the 'initialize' handshake
            // or 'tools/list') resolve to no endpoint, but must still be processed and authenticated.
            McpEndpoint mcpEndpoint =
                    mcpEndpointResolver.resolve(requestAsString).orElse(null);

            executeInContext(wrapper, response, filterChain, new McpRequestContext(mcpEndpoint));
        }
    }

    private void processExternalWebRequest(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain, String servletPath)
            throws IOException, ServletException {
        String relativePath = servletPath.substring(WebAutoConfiguration.EXTERNAL_API_PATH.length());

        Optional<MasterWebEndpoint> masterWebEndpoint =
                masterWebEndpointResolver.resolveEndpoint(relativePath, HttpMethod.valueOf(request.getMethod()));

        if (masterWebEndpoint.isPresent()) {
            ExternalWebRequestContext endpoint = new ExternalWebRequestContext(masterWebEndpoint.get());
            executeInContext(request, response, filterChain, endpoint);
        } else {
            throw new ServletException("Unrecognized endpoint got invoked. Failing request fast");
        }
    }

    private static void executeInContext(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain,
            MasterRequestContext endpoint)
            throws IOException, ServletException {

        try {
            ScopedValue.where(MASTER_REQUEST_CONTEXT, endpoint).call(() -> {
                filterChain.doFilter(request, response);
                return null;
            });
        } catch (IOException | ServletException e) {
            throw e;
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public static ExternalWebRequestContext requireWebRequestContext() {
        return getWebRequestContext()
                .orElseThrow(() ->
                        new IllegalStateException("Expect ExternalWebRequestContext to be bounded to the thread"));
    }

    public static McpRequestContext requireMcpRequestContext() {
        return getMcpRequestContext()
                .orElseThrow(() -> new IllegalStateException("Expect McpRequestContext to be bounded to the thread"));
    }

    public static Optional<ExternalWebRequestContext> getWebRequestContext() {
        if (!MASTER_REQUEST_CONTEXT.isBound()) {
            return Optional.empty();
        }

        return Optional.ofNullable(MASTER_REQUEST_CONTEXT.get())
                .filter(it -> it instanceof ExternalWebRequestContext)
                .map(it -> (ExternalWebRequestContext) it);
    }

    public static Optional<McpRequestContext> getMcpRequestContext() {
        if (!MASTER_REQUEST_CONTEXT.isBound()) {
            return Optional.empty();
        }

        return Optional.ofNullable(MASTER_REQUEST_CONTEXT.get())
                .filter(it -> it instanceof McpRequestContext)
                .map(it -> (McpRequestContext) it);
    }
}
