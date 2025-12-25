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

import com.nucleonforge.axile.common.auth.core.User;
import com.nucleonforge.axile.master.exception.auth.UserNotFoundException;

/**
 * SPI interface that is capable to load the {@link User} by his/her username.
 *
 * @since 16.07.25
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
public interface UserProvider {

    /**
     * Load user by username.
     *
     * @param username by which the user will be loaded.
     * @return the loaded {@link User}.
     * @throws UserNotFoundException in case the user is not found.
     */
    User load(String username) throws UserNotFoundException;
}
