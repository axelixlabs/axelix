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

import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import jakarta.servlet.http.HttpServletRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.axelixlabs.axelix.master.api.error.SimpleApiError;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.request.LoginRequest;
import com.axelixlabs.axelix.master.api.external.response.UserProfileResponse;
import com.axelixlabs.axelix.master.service.auth.CookieService;
import com.axelixlabs.axelix.master.service.auth.UserLoginService;

/**
 * The API for working with users.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
@Tag(name = "API for working with Users", description = "The endpoints for user login and authentication")
@ExternalApiRestController
@RequestMapping(path = ApiPaths.UsersApi.MAIN)
public class UserApi {

    private final UserLoginService userLoginService;
    private final CookieService cookieService;

    public UserApi(UserLoginService userLoginService, CookieService cookieService) {
        this.userLoginService = userLoginService;
        this.cookieService = cookieService;
    }

    /**
     * Login the user.
     *
     * @param loginRequest request for login
     * @return the HTTP Response with the Authorization header
     */
    @Operation(
            summary = "Log-in by the username/password combination",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        headers = {
                            @Header(
                                    name = "Set-Cookie",
                                    required = true,
                                    description = "The JWT token that should be subsequently used for auth purposes")
                        }),
                @ApiResponse(
                        description = "Bad Request",
                        responseCode = "400",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class))),
                @ApiResponse(
                        description = "Unauthorized. Most likely the credentials pair username/password is wrong",
                        responseCode = "401"),
                @ApiResponse(description = "Forbidden. The access into the system is forbidden", responseCode = "403"),
                @ApiResponse(
                        description = "Internal Server Error",
                        responseCode = "500",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class)))
            })
    @PostMapping(path = ApiPaths.UsersApi.LOGIN)
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String token = userLoginService.login(loginRequest.username(), loginRequest.password());

        ResponseCookie cookie = cookieService.buildAuthCookie(token);

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    /**
     * Logout current user.
     *
     * @return the HTTP Response with expired aut token cookie.
     */
    @Operation(
            summary = "Log-out current user",
            responses = {
                @ApiResponse(
                        description = "OK",
                        responseCode = "200",
                        headers = {@Header(name = "Set-Cookie", required = true, description = "The expired cookie")}),
                @ApiResponse(
                        description = "Bad Request",
                        responseCode = "400",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class))),
                @ApiResponse(description = "Unauthorized", responseCode = "401"),
                @ApiResponse(
                        description = "Internal Server Error",
                        responseCode = "500",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = SimpleApiError.class)))
            })
    @PostMapping(path = ApiPaths.UsersApi.LOGOUT)
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = cookieService.buildExpiredAuthCookie();

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    /**
     * Extracts the profile from the HTTP Request.
     *
     * @return the profile of the given user.
     */
    public UserProfileResponse getProfile(HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }
}
