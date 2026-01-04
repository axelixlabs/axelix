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
package com.nucleonforge.axelix.sbs.spring.auth;

import com.nucleonforge.axelix.common.auth.core.AuthorizationRequest;
import com.nucleonforge.axelix.common.auth.core.DecodedUser;
import com.nucleonforge.axelix.common.auth.core.User;

/**
 * SPI interface that is capable of authorizing the given {@link User} against an {@link AuthorizationRequest}.
 *
 * @since 16.07.25
 * @author Mikhail Polivakha
 */
public interface Authorizer {

    /**
     * Authorizes the given {@link User} against the specified {@link AuthorizationRequest}.
     *
     * @param user the user to authorize
     * @param authorizationRequest the request containing required authorities
     * @throws AuthorizationException if access is denied
     */
    void authorize(DecodedUser user, AuthorizationRequest authorizationRequest) throws AuthorizationException;
}
