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
package com.nucleonforge.axelix.master.service.auth.provider;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.nucleonforge.axelix.common.auth.core.DefaultAuthority;
import com.nucleonforge.axelix.common.auth.core.DefaultRole;
import com.nucleonforge.axelix.common.auth.core.DefaultUser;
import com.nucleonforge.axelix.common.auth.core.User;
import com.nucleonforge.axelix.master.autoconfiguration.auth.StaticAdminCredentialsProperties;
import com.nucleonforge.axelix.master.exception.auth.UserNotFoundException;
import com.nucleonforge.axelix.master.service.auth.UserLoginService;

/**
 * {@link UserLoginService} that authenticates a given user by the static pair of the username/password.
 *
 * @author Mikhail Polivakha
 */
public class StaticAdminUserProvider implements UserProvider {

    private static final String ADMIN_ROLE = "ADMIN";

    private final StaticAdminCredentialsProperties staticCredentialsConfig;

    public StaticAdminUserProvider(StaticAdminCredentialsProperties staticCredentialsConfig) {
        this.staticCredentialsConfig = staticCredentialsConfig;
    }

    @Override
    public User load(String username) throws UserNotFoundException {
        if (Objects.equals(staticCredentialsConfig.getUsername(), username)) {
            return new DefaultUser(
                    username,
                    staticCredentialsConfig.getPassword(),
                    Set.of(new DefaultRole(
                            ADMIN_ROLE, Arrays.stream(DefaultAuthority.values()).collect(Collectors.toSet()))));
        } else {
            throw new UserNotFoundException(username);
        }
    }
}
