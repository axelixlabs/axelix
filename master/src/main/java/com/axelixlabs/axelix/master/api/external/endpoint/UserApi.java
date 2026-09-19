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
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.request.LoginRequest;
import com.axelixlabs.axelix.master.api.external.response.UserResponse;
import com.axelixlabs.axelix.master.exception.auth.InvalidCredentialsException;
import com.axelixlabs.axelix.master.service.auth.CookieService;
import com.axelixlabs.axelix.master.service.auth.provider.UserAuthenticator;
import com.axelixlabs.axelix.master.service.state.auth.UserService;

/**
 * The API for working with users.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@ExternalApiRestController
public class UserApi {

    private static final Logger log = LoggerFactory.getLogger(UserApi.class);

    private final CookieService cookieService;
    private final UserAuthenticator userAuthenticator;
    private final JwtEncoderService jwtEncoderService;
    private final UserService userService;

    private static final InvalidCredentialsException INVALID_CREDENTIALS_EXCEPTION = new InvalidCredentialsException();

    public UserApi(
            CookieService cookieService,
            UserAuthenticator userAuthenticator,
            JwtEncoderService jwtEncoderService,
            UserService userService) {
        this.cookieService = cookieService;
        this.userAuthenticator = userAuthenticator;
        this.jwtEncoderService = jwtEncoderService;
        this.userService = userService;
    }

    @GetMapping(path = ApiPaths.UsersApi.USERS_FEED)
    public ResponseEntity<List<UserResponse>> getUsersFeed() {
        // TODO:
        //  Okay, I know what you're thinking. But the assumption is that the amount of users will not be that high, and
        // it is
        //  okay to load them in this way.
        Map<String, Set<String>> roleNamesByUserId = userService.findAllRoleNamesByUserId();

        List<UserResponse> users = userService.findAll().stream()
                .map(user -> UserResponse.from(user, roleNamesByUserId.getOrDefault(user.id(), Set.of())))
                .toList();

        return ResponseEntity.ok(users);
    }

    @GetMapping(path = ApiPaths.UsersApi.GET_USER_BY_ID)
    public ResponseEntity<UserResponse> getUser(@PathVariable("userId") String userId) {
        return userService
                .findUserById(userId)
                .map(user -> UserResponse.from(user, userService.findRoleNamesByUserId(user.id())))
                .map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("User with ID was not found {}", userId);
                    return ResponseEntity.notFound().build();
                });
    }

    @PostMapping(path = ApiPaths.UsersApi.LOGIN)
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        User user = userAuthenticator.authenticate(loginRequest.username(), loginRequest.password());

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

    @PostMapping(path = ApiPaths.UsersApi.LOGOUT)
    public ResponseEntity<?> logout() {
        ResponseCookie authCookie = cookieService.buildExpiredAuthCookie();
        ResponseCookie authoritiesCookie = cookieService.buildExpiredAuthMetadataCookie();

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, authCookie.toString())
                .header(HttpHeaders.SET_COOKIE, authoritiesCookie.toString())
                .build();
    }
}
