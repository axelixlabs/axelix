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

import org.assertj.core.api.Assertions;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.axelixlabs.axelix.master.api.external.response.InstancesGridResponse.InstanceShortProfile;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.service.convert.response.InstancesToShortProfileConverter;

/**
 * Unit tests for {@link InstancesToShortProfileConverter}.
 *
 * @author Mikhail Polivakha
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = InstancesToShortProfileConverter.class)
class InstancesToShortProfileConverterTest {

    @Autowired
    private InstancesToShortProfileConverter subject;

    @Test
    void shouldConvertInstanceToItsShortProfile() {
        // given.
        Instance input = Instancio.create(Instance.class);

        // when.
        InstanceShortProfile result = subject.convert(input);

        // then.
        Assertions.assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("deployedFor", "instanceId")
                .isEqualTo(input);
    }
}
