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
package com.axelixlabs.axelix.master.service.export;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.master.model.instance.InstanceId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link DefaultStateArchiveFileNameGenerator}.
 *
 * @author Mikhail Polivakha
 */
class DefaultStateArchiveFileNameGeneratorTest {

    private DefaultStateArchiveFileNameGenerator subject;

    @BeforeEach
    void setUp() {
        subject = new DefaultStateArchiveFileNameGenerator();
    }

    @Test
    void shouldGenerateValidFileName() {
        // given.
        String instanceId = "ims-service-k02i302k-od20w";

        // when.
        String filename = subject.generate(InstanceId.of(instanceId));

        // then.
        assertThat(filename).contains(instanceId);
    }
}
