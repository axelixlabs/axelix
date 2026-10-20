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
package com.axelixlabs.axelix.sbs.spring.core.auth;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import com.axelixlabs.axelix.common.auth.core.AuthenticationSchemes;
import com.axelixlabs.axelix.common.auth.core.DefaultSecurityContext;
import com.axelixlabs.axelix.common.auth.core.SecurityContext;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.exception.AuthorizationException;
import com.axelixlabs.axelix.common.auth.exception.ExpiredJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.InvalidJwtTokenException;
import com.axelixlabs.axelix.common.auth.exception.JwtParsingException;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;

/**
 * Reactive counterpart of {@link JwtAuthorizationFilter} for WebFlux applications.
 *
 * @author Nikita Kirillov
 * @since 20.10.2026
 */
public class ReactiveJwtAuthorizationFilter implements WebFilter {

    private final WebIdentityAccessManager webIdentityAccessManager;
    private final String baseActuatorPath;
    private final PathPattern pathPattern;

    public ReactiveJwtAuthorizationFilter(WebIdentityAccessManager webIdentityAccessManager, String baseActuatorPath) {
        this.webIdentityAccessManager = webIdentityAccessManager;
        this.baseActuatorPath = baseActuatorPath;
        this.pathPattern = PathPatternParser.defaultInstance.parse(baseActuatorPath + "/axelix-*/**");
    }

    @Override
    @SuppressWarnings("NullAway")
    public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        PathContainer path = request.getPath().pathWithinApplication();

        if (!pathPattern.matches(path)) {
            return chain.filter(exchange);
        }

        SecurityContext securityContext;
        try {
            String token = resolveToken(request);
            HttpMethod requestHttpMethod =
                    HttpMethod.valueOf(request.getMethod().name());
            String relativePath = normalizedPath(path).substring(baseActuatorPath.length());
            User user = webIdentityAccessManager.verifyAccess(relativePath, requestHttpMethod, token);
            securityContext = new DefaultSecurityContext(user, token);
        } catch (JwtParsingException | ExpiredJwtTokenException | InvalidJwtTokenException e) {
            return respondWith(exchange.getResponse(), HttpStatus.UNAUTHORIZED, e.getMessage());
        } catch (AuthorizationException e) {
            return respondWith(exchange.getResponse(), HttpStatus.FORBIDDEN, e.getMessage());
        }

        AxelixPrincipal principal = new AxelixPrincipal(securityContext);
        ServerWebExchange axelixExchange = exchange.mutate()
                .request(new BoundedElasticBodyRequest(request))
                .principal(Mono.just(principal))
                .build();

        return chain.filter(axelixExchange).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Returns the path the way WebFlux matches it against the endpoints.
     */
    private static String normalizedPath(PathContainer pathContainer) {
        StringBuilder path = new StringBuilder();
        List<PathContainer.Element> elements = pathContainer.elements();

        for (PathContainer.Element element : elements) {
            path.append(
                    element instanceof PathContainer.PathSegment segment ? segment.valueToMatch() : element.value());
        }
        return path.toString();
    }

    @Nullable
    private String resolveToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String bearerSchemeCode = AuthenticationSchemes.BEARER.prefix();
        if (header != null && header.startsWith(bearerSchemeCode)) {
            return header.substring(bearerSchemeCode.length());
        }
        return null;
    }

    private Mono<Void> respondWith(ServerHttpResponse response, HttpStatus status, @Nullable String message) {
        response.setStatusCode(status);
        byte[] body = message == null ? new byte[0] : message.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body)));
    }

    /**
     * Delivers the request body off the event loop, since the endpoint is invoked once the body is read.
     */
    private static class BoundedElasticBodyRequest extends ServerHttpRequestDecorator {

        BoundedElasticBodyRequest(ServerHttpRequest delegate) {
            super(delegate);
        }

        @Override
        public Flux<DataBuffer> getBody() {
            return super.getBody().publishOn(Schedulers.boundedElastic());
        }
    }
}
