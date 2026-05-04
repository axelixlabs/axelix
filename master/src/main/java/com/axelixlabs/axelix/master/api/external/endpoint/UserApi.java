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
package com.axelixlabs.axelix.master.api.external.endpoint;

import java.util.List;

import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.request.LoginRequest;
import com.axelixlabs.axelix.master.api.external.swagger.DefaultApiResponse;
import com.axelixlabs.axelix.master.exception.auth.InvalidCredentialsException;
import com.axelixlabs.axelix.master.service.auth.CookieService;
import com.axelixlabs.axelix.master.service.auth.provider.UserAuthenticator;

/**
 * The API for working with users.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@Tag(name = "API for working with Users", description = "The endpoints for user login and authentication")
@ExternalApiRestController
@ConditionalOnProperty(prefix = "axelix.master.auth.options.static-admin", name = "enabled", havingValue = "true")
public class UserApi {

    private final CookieService cookieService;
    private final List<UserAuthenticator> userAuthenticators;
    private final JwtEncoderService jwtEncoderService;

    private static final InvalidCredentialsException INVALID_CREDENTIALS_EXCEPTION = new InvalidCredentialsException();

    public UserApi(
            CookieService cookieService,
            List<UserAuthenticator> userAuthenticators,
            JwtEncoderService jwtEncoderService) {
        this.cookieService = cookieService;
        this.userAuthenticators = userAuthenticators;
        this.jwtEncoderService = jwtEncoderService;
    }

    /**
     * Login the user.
     *
     * @param loginRequest request for login
     * @return the HTTP Response with the Authorization header
     */
    @DefaultApiResponse(summary = "Log-in by the username/password combination")
    @ApiResponse(
            description = "OK",
            responseCode = "200",
            headers = {
                @Header(
                        name = "Set-Cookie",
                        required = true,
                        description = "The JWT token that should be subsequently used for auth purposes")
            })
    @ApiResponse(
            description = "Unauthorized. Most likely the credentials pair username/password is wrong",
            responseCode = "401")
    @ApiResponse(description = "Forbidden. The access into the system is forbidden", responseCode = "403")
    @PostMapping(path = ApiPaths.UsersApi.LOGIN)
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {

        User user = null;
        for (UserAuthenticator userAuthenticator : userAuthenticators) {
            user = userAuthenticator.authenticate(loginRequest.username(), loginRequest.password());
            if (user != null) {
                break;
            }
        }

        if (user == null) {
            throw INVALID_CREDENTIALS_EXCEPTION;
        }

        String token = jwtEncoderService.generateToken(user);

        ResponseCookie cookie = cookieService.buildAuthCookie(token);
        ResponseCookie cookieAuthorities = cookieService.buildAuthoritiesMetadataCookie(user.getRoles());

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .header(HttpHeaders.SET_COOKIE, cookieAuthorities.toString())
                .build();
    }

    /**
     * Logout current user.
     *
     * @return the HTTP Response with expired aut token cookie.
     */
    @DefaultApiResponse(summary = "Log-out current user")
    @ApiResponse(
            description = "OK",
            responseCode = "200",
            headers = {@Header(name = "Set-Cookie", required = true, description = "The expired cookie")})
    @ApiResponse(description = "Unauthorized", responseCode = "401")
    @PostMapping(path = ApiPaths.UsersApi.LOGOUT)
    public ResponseEntity<?> logout() {
        ResponseCookie authCookie = cookieService.buildExpiredAuthCookie();
        ResponseCookie authoritiesCookie = cookieService.buildExpiredAuthCookie();

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, authCookie.toString())
                .header(HttpHeaders.SET_COOKIE, authoritiesCookie.toString())
                .build();
    }
}
