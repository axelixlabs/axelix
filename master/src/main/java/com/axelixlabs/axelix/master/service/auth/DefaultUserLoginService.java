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
package com.axelixlabs.axelix.master.service.auth;

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.exception.auth.AuthenticationException;
import com.axelixlabs.axelix.master.exception.auth.InvalidCredentialsException;
import com.axelixlabs.axelix.master.service.auth.jwt.JwtEncoderService;
import com.axelixlabs.axelix.master.service.auth.provider.UserProvider;

/**
 * Default {@link UserLoginService}.
 *
 * @author Mikhail Polivakha
 */
@Service
public class DefaultUserLoginService implements UserLoginService {

    private final JwtEncoderService jwtEncoderService;
    private final UserProvider userProvider;

    private static final InvalidCredentialsException INVALID_CREDENTIALS_EXCEPTION = new InvalidCredentialsException();

    protected DefaultUserLoginService(JwtEncoderService jwtEncoderService, UserProvider userProvider) {
        this.jwtEncoderService = jwtEncoderService;
        this.userProvider = userProvider;
    }

    @Override
    public String login(String username, String password) throws AuthenticationException {
        User user = userProvider.load(username);

        // TODO: password should be encoded. We need a password encoder or something
        if (Objects.equals(user.getPassword(), password)) {
            return jwtEncoderService.generateToken(user);
        } else {
            throw INVALID_CREDENTIALS_EXCEPTION;
        }
    }
}
