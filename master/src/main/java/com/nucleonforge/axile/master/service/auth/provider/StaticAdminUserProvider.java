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
package com.nucleonforge.axile.master.service.auth.provider;

import java.util.Objects;
import java.util.Set;

import com.nucleonforge.axile.common.auth.core.DefaultAuthority;
import com.nucleonforge.axile.common.auth.core.DefaultRole;
import com.nucleonforge.axile.common.auth.core.DefaultUser;
import com.nucleonforge.axile.common.auth.core.User;
import com.nucleonforge.axile.master.autoconfiguration.auth.StaticAdminCredentialsProperties;
import com.nucleonforge.axile.master.exception.auth.UserNotFoundException;
import com.nucleonforge.axile.master.service.auth.UserLoginService;

/**
 * {@link UserLoginService} that authenticates a given user by the static pair of the username/password.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
public class StaticAdminUserProvider implements UserProvider {

    private final StaticAdminCredentialsProperties staticCredentialsConfig;

    public StaticAdminUserProvider(StaticAdminCredentialsProperties staticCredentialsConfig) {
        this.staticCredentialsConfig = staticCredentialsConfig;
    }

    @Override
    public User load(String username) throws UserNotFoundException {
        if (Objects.equals(staticCredentialsConfig.getUsername(), username)) {
            // TODO: We need to revisit our roles API here.
            return new DefaultUser(
                    username,
                    staticCredentialsConfig.getPassword(),
                    Set.of(new DefaultRole(
                            "ADMIN",
                            Set.of(
                                    DefaultAuthority.BEANS,
                                    DefaultAuthority.CACHES,
                                    DefaultAuthority.HEALTH,
                                    DefaultAuthority.INFO,
                                    DefaultAuthority.CONDITIONS,
                                    DefaultAuthority.CONFIGPROPS,
                                    DefaultAuthority.DETAILS,
                                    DefaultAuthority.ENV,
                                    DefaultAuthority.HEAP_DUMP,
                                    DefaultAuthority.THREAD_DUMP,
                                    DefaultAuthority.MAPPINGS,
                                    DefaultAuthority.METRICS,
                                    DefaultAuthority.LOGGERS,
                                    DefaultAuthority.SCHEDULED_TASKS,
                                    DefaultAuthority.SCHEDULED_TASKS_MANAGEMENT,
                                    DefaultAuthority.PROFILE_MANAGEMENT,
                                    DefaultAuthority.PROPERTY_MANAGEMENT),
                            Set.of())));
        } else {
            throw new UserNotFoundException(username);
        }
    }
}
