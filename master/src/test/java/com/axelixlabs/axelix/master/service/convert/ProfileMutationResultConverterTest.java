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
package com.axelixlabs.axelix.master.service.convert;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.ProfileMutationResult;
import com.axelixlabs.axelix.master.api.external.response.ProfileUpdateResponse;
import com.axelixlabs.axelix.master.service.convert.response.ProfileMutationResultConverter;

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
