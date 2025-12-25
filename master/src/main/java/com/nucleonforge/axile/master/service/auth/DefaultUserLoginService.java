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

import java.util.Objects;

import org.springframework.stereotype.Service;

import com.nucleonforge.axile.common.auth.core.User;
import com.nucleonforge.axile.master.exception.auth.AuthenticationException;
import com.nucleonforge.axile.master.exception.auth.InvalidCredentialsException;
import com.nucleonforge.axile.master.service.auth.jwt.JwtEncoderService;
import com.nucleonforge.axile.master.service.auth.provider.UserProvider;

/**
 * Default {@link UserLoginService}.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
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
        if (Objects.equals(user.password(), password)) {
            return jwtEncoderService.generateToken(user);
        } else {
            throw INVALID_CREDENTIALS_EXCEPTION;
        }
    }
}
