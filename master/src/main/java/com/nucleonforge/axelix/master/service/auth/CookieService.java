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
package com.nucleonforge.axelix.master.service.auth;

import org.springframework.http.ResponseCookie;

/**
 * Service interface for authentication HTTP cookie operations.
 *
 * @since 12.12.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public interface CookieService {

    /**
     * Builds an authentication HTTP cookie with the provided JWT token.
     *
     * @param token JWT token to be stored in the cookie
     * @return configured ResponseCookie instance ready to be set in HTTP response
     */
    ResponseCookie buildAuthCookie(String token);

    /**
     * Builds an expired authentication HTTP cookie.
     * <p>
     * The value of the token
     *
     * @return configured ResponseCookie instance ready to be set in HTTP response
     */
    ResponseCookie buildExpiredAuthCookie();
}
