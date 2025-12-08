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
package com.nucleonforge.axile.master.service.convert;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.ProfileMutationResult;
import com.nucleonforge.axile.master.api.response.ProfileUpdateResponse;
import com.nucleonforge.axile.master.service.convert.response.ProfileMutationResultConverter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ProfileMutationResultConverter}
 *
 * @since 25.09.2025
 * @author Nikita Kirillov
 */
class ProfileMutationResultConverterTest {

    private final ProfileMutationResultConverter subject = new ProfileMutationResultConverter();

    @Test
    void testConvertHappyPath() {
        ProfileMutationResult profileMutationResult =
                new ProfileMutationResult(true, "New profiles have been activated");

        // when.
        ProfileUpdateResponse response = subject.convertInternal(profileMutationResult);

        // then.
        assertThat(response.updated()).isTrue();
        assertThat(response.reason()).isEqualTo("New profiles have been activated");
    }
}
