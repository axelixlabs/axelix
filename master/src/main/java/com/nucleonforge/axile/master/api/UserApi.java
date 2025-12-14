/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.master.api;

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
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.master.api.error.SimpleApiError;
import com.nucleonforge.axile.master.api.request.LoginRequest;
import com.nucleonforge.axile.master.api.response.UserProfileResponse;
import com.nucleonforge.axile.master.service.auth.CookieService;
import com.nucleonforge.axile.master.service.auth.UserLoginService;

/**
 * The API for working with users.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
@Tag(name = "API for working with Users", description = "The endpoints for user login and authentication")
@RestController
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
     * Extracts the profile from the HTTP Request.
     *
     * @return the profile of the given user.
     */
    public UserProfileResponse getProfile(HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }
}
