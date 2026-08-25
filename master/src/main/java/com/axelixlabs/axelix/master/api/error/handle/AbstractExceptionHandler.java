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
package com.axelixlabs.axelix.master.api.error.handle;

import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.util.ProxyUtils;

import com.axelixlabs.axelix.common.auth.exception.AuthorizationException;
import com.axelixlabs.axelix.master.filter.auth.requestcontext.ExternalWebRequestContext;
import com.axelixlabs.axelix.master.filter.auth.requestcontext.MasterRequestContextInitFilter;
import com.axelixlabs.axelix.master.filter.auth.requestcontext.McpRequestContext;
import com.axelixlabs.axelix.master.service.auth.intercept.mcp.OnMcpAccessDenied;
import com.axelixlabs.axelix.master.service.auth.intercept.mcp.OnMcpAuthenticationFailure;
import com.axelixlabs.axelix.master.service.auth.intercept.mcp.OnMcpIamEventInterceptor;
import com.axelixlabs.axelix.master.service.auth.intercept.web.OnWebAccessDenied;
import com.axelixlabs.axelix.master.service.auth.intercept.web.OnWebAuthenticationFailure;
import com.axelixlabs.axelix.master.service.auth.intercept.web.OnWebIamEventInterceptor;

/**
 * Abstract implementation of {@link ExceptionHandler}.
 *
 * @author Mikhail Polivakha
 */
public abstract class AbstractExceptionHandler<T extends Exception> implements ExceptionHandler<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractExceptionHandler.class);

    private final List<OnWebAccessDenied> onWebAccessDeniedInterceptors;
    private final List<OnWebAuthenticationFailure> onWebAuthenticationFailureInterceptors;

    private final List<OnMcpAccessDenied> onMcpAccessDeniedInterceptors;
    private final List<OnMcpAuthenticationFailure> onMcpAuthenticationFailureInterceptors;

    protected AbstractExceptionHandler(
            List<OnWebIamEventInterceptor> webInterceptors, List<OnMcpIamEventInterceptor> mcpInterceptors) {
        this.onWebAccessDeniedInterceptors = getInterceptorsOfType(webInterceptors, OnWebAccessDenied.class);
        this.onWebAuthenticationFailureInterceptors =
                getInterceptorsOfType(webInterceptors, OnWebAuthenticationFailure.class);
        this.onMcpAccessDeniedInterceptors = getInterceptorsOfType(mcpInterceptors, OnMcpAccessDenied.class);
        this.onMcpAuthenticationFailureInterceptors =
                getInterceptorsOfType(mcpInterceptors, OnMcpAuthenticationFailure.class);
    }

    protected void fireOnAccessDenied(HttpServletRequest request, AuthorizationException exception) {
        fireOnWebAccessDenied(request, exception);
        fireOnMcpAccessDenied(request, exception);
    }

    protected void fireOnAuthenticationFailure(HttpServletRequest request) {
        Optional<ExternalWebRequestContext> webRequestContext = MasterRequestContextInitFilter.getWebRequestContext();

        webRequestContext.ifPresent(context -> {
            onWebAuthenticationFailureInterceptors.forEach(interceptor -> {
                interceptor.onAuthenticationFailure(context.masterWebEndpoint(), request);
            });
        });

        Optional<McpRequestContext> mcpRequestContext = MasterRequestContextInitFilter.getMcpRequestContext();

        mcpRequestContext.ifPresent(context -> {
            onMcpAuthenticationFailureInterceptors.forEach(interceptor -> {
                interceptor.onAuthenticationFailure(context.mcpEndpoint(), request);
            });
        });
    }

    private void fireOnWebAccessDenied(HttpServletRequest request, AuthorizationException exception) {
        Optional<ExternalWebRequestContext> webRequestContext = MasterRequestContextInitFilter.getWebRequestContext();

        webRequestContext.ifPresent(context -> {
            onWebAccessDeniedInterceptors.forEach(interceptor -> {
                interceptor.onAccessDenied(context.masterWebEndpoint(), request, exception.getUser());
            });
        });
    }

    private void fireOnMcpAccessDenied(HttpServletRequest request, AuthorizationException exception) {
        Optional<McpRequestContext> mcpRequestContext = MasterRequestContextInitFilter.getMcpRequestContext();

        mcpRequestContext.ifPresent(context -> {

            // McpEndpoint might be null only in case the MCP-protocol specific endpoint was called.
            // Such endpoints are always assumed to be authorized for any user. So, here, if we have the
            // AuthorizationException, we must have mcpEndpoint at this point.
            if (context.mcpEndpoint() != null) {
                onMcpAccessDeniedInterceptors.forEach(interceptor -> {
                    interceptor.onAccessDenied(context.mcpEndpoint(), request, exception.getUser());
                });
            } else {
                log.warn("McpEndpoint must exist at this point. Please, report this error to maintainers", exception);
            }
        });
    }

    private static <T> List<T> getInterceptorsOfType(List<?> interceptors, Class<T> interceptorType) {
        return interceptors.stream()
                .filter(it -> interceptorType.isAssignableFrom(ProxyUtils.getUserClass(it.getClass())))
                .map(interceptorType::cast)
                .toList();
    }
}
