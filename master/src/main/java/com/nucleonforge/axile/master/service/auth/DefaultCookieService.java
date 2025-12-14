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
package com.nucleonforge.axile.master.service.auth;

import org.springframework.http.ResponseCookie;

import com.nucleonforge.axile.master.autoconfiguration.auth.CookieProperties;
import com.nucleonforge.axile.master.autoconfiguration.auth.JwtProperties;

/**
 * Default implementation of {@link CookieService}.
 */
public class DefaultCookieService implements CookieService {

    private final CookieProperties cookieProperties;

    private final JwtProperties jwtProperties;

    public DefaultCookieService(CookieProperties cookieProperties, JwtProperties jwtProperties) {
        this.cookieProperties = cookieProperties;
        this.jwtProperties = jwtProperties;
    }

    public ResponseCookie buildAuthCookie(String token) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieProperties.getName(), token)
                .httpOnly(true)
                .secure(cookieProperties.isSecure())
                .path("/")
                .maxAge(jwtProperties.getLifespan())
                .sameSite("Strict");

        return builder.build();
    }
}
