/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.sbs.spring.profiles;

import org.springframework.context.ApplicationContext;

/**
 * Interface responsible for replacing the active Spring profiles at runtime.
 * <p>
 * Implementations must ensure that any profile modification is properly applied by
 * updating the environment and restarting the {@link ApplicationContext}
 * to reflect the changes across the application.
 * </p>
 *
 * @author Nikita Kirillov
 * @since 11.07.2025
 */
public interface ProfileMutator {

    /**
     * Replaces the current set of active profiles with the provided array.
     * <p>
     * All existing active profiles will be removed, and only the specified ones will be activated.
     * </p>
     *
     * @param profiles the new set of profiles to activate
     * @return the result of the profile replacement operation
     */
    ProfileMutationResponse replaceActiveProfiles(String[] profiles);
}
