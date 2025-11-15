package com.nucleonforge.axile.sbs.auth.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.http.HttpHeaders;
import org.springframework.web.filter.OncePerRequestFilter;

import com.nucleonforge.axile.common.auth.core.Authority;
import com.nucleonforge.axile.common.auth.core.AuthorizationRequest;
import com.nucleonforge.axile.sbs.auth.AuthorizationException;
import com.nucleonforge.axile.sbs.auth.model.DecodedUser;
import com.nucleonforge.axile.sbs.auth.spi.Authorizer;
import com.nucleonforge.axile.sbs.auth.spi.jwt.exception.ExpiredJwtTokenException;
import com.nucleonforge.axile.sbs.auth.spi.jwt.exception.InvalidJwtTokenException;
import com.nucleonforge.axile.sbs.auth.spi.jwt.exception.JwtParsingException;
import com.nucleonforge.axile.sbs.auth.spi.jwt.exception.JwtTokenDecodingException;
import com.nucleonforge.axile.sbs.auth.spi.jwt.service.JwtDecoderService;

/**
 * A custom servlet filter that restricts access to Actuator endpoints based on JWT token presence, validity,
 * and mapped {@link Authority} authorities.
 * <p>
 * Rejects unauthorized requests before they reach the application logic.
 *
 * @author Nikita Kirillov
 * @since 29.07.2025
 */
@SuppressWarnings("NullAway") // TODO: Pending issue GH-42 – introduce exception translator and refactor this filter
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtDecoderService jwtDecoderService;

    private final AuthorityResolver defaultAuthorityResolver;

    private final Authorizer authorizer;

    public JwtAuthorizationFilter(
            JwtDecoderService jwtDecoderService, AuthorityResolver defaultAuthorityResolver, Authorizer authorizer) {
        this.jwtDecoderService = jwtDecoderService;
        this.defaultAuthorityResolver = defaultAuthorityResolver;
        this.authorizer = authorizer;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (token == null) {
            // TODO: it is wrong. If token is not present then the response should be unauthorized
            respondWith(response, HttpServletResponse.SC_FORBIDDEN, "Authorization token is missing");
            return;
        }

        String requestPath = request.getRequestURI();

        try {
            DecodedUser user = jwtDecoderService.decodeTokenToUser(token);
            Optional<Authority> requiredOpt = defaultAuthorityResolver.resolve(requestPath);

            AuthorizationRequest authorizationRequest =
                    new AuthorizationRequest(requiredOpt.map(Set::of).orElse(Collections.emptySet()));

            authorizer.authorize(user, authorizationRequest);

            filterChain.doFilter(request, response);

        } catch (JwtParsingException | ExpiredJwtTokenException | InvalidJwtTokenException e) {
            respondWith(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } catch (AuthorizationException e) {
            respondWith(response, HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (JwtTokenDecodingException e) {
            respondWith(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Nullable
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void respondWith(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.getWriter().write(message);
        response.getWriter().flush();
    }
}
